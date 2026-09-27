package com.sushishop.security.jwt;

import com.sushishop.order.OrderRepository;
import com.sushishop.security.Roles;
import com.sushishop.user.CustomUserDetails;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String NEW_ORDERS_TOPIC = "/topic/orders/new";
    private static final Pattern ORDER_TOPIC_PATTERN = Pattern.compile("^/topic/orders/(\\d+)$");
    private static final String ROLE_ADMIN = "ROLE_" + Roles.ADMIN;
    private static final String ROLE_COURIER = "ROLE_" + Roles.COURIER;

    private final WsTicketService wsTicketService;
    private final UserCacheService userCacheService;
    private final OrderRepository orderRepository;

    @Override
    @Nonnull
    public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        var command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            accessor.setUser(authenticate(accessor));
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }

        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        var header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadCredentialsException("Missing WebSocket authentication ticket");
        }

        var ticket = header.substring(7);
        var payload = wsTicketService.consume(ticket)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired WebSocket authentication ticket"));

        var cachedUser = userCacheService.getCachedUser(payload.email(), payload.tokenVersion());
        if (cachedUser == null) {
            throw new BadCredentialsException("Unknown user for WebSocket authentication");
        }

        var principal = new CustomUserDetails(cachedUser);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth)) {
            throw new AccessDeniedException("Not authenticated");
        }

        var destination = accessor.getDestination();
        if (NEW_ORDERS_TOPIC.equals(destination)) {
            requireStaff(auth);
            return;
        }

        var matcher = destination == null ? null : ORDER_TOPIC_PATTERN.matcher(destination);
        if (matcher == null || !matcher.matches()) {
            throw new AccessDeniedException("Subscription destination is not allowed");
        }
        authorizeOrderTopic(auth, Long.valueOf(matcher.group(1)));
    }

    private void authorizeOrderTopic(UsernamePasswordAuthenticationToken auth, Long orderId) {
        if (isStaff(auth)) {
            return;
        }

        var ownerEmail = orderRepository.findOwnerEmailById(orderId)
                .orElseThrow(() -> new AccessDeniedException("Order not found"));

        var principal = (CustomUserDetails) auth.getPrincipal();
        if (!ownerEmail.equals(principal.getUsername())) {
            throw new AccessDeniedException("Not allowed to subscribe to this order");
        }
    }

    private void requireStaff(UsernamePasswordAuthenticationToken auth) {
        if (!isStaff(auth)) {
            throw new AccessDeniedException("Requires staff role");
        }
    }

    private boolean isStaff(UsernamePasswordAuthenticationToken auth) {
        return hasRole(auth, ROLE_ADMIN) || hasRole(auth, ROLE_COURIER);
    }

    private boolean hasRole(UsernamePasswordAuthenticationToken auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
