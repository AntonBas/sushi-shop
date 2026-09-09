package com.sushishop.security.jwt;

import com.sushishop.order.OrderRepository;
import com.sushishop.user.CustomUserDetails;
import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserRole;
import com.sushishop.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtChannelInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private JwtChannelInterceptor interceptor;

    private final MessageChannel channel = mock(MessageChannel.class);

    private User buildUser(String email, UserRole role) {
        return User.builder().id(1L).email(email).name("Test").userRole(role).build();
    }

    private UsernamePasswordAuthenticationToken authFor(String email, UserRole role) {
        var principal = new CustomUserDetails(buildUser(email, role));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Message<byte[]> connectMessage(String token) {
        var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (token != null) {
            accessor.setNativeHeader("Authorization", "Bearer " + token);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> subscribeMessage(String destination, UsernamePasswordAuthenticationToken user) {
        var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setUser(user);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void shouldRejectConnectWithoutToken() {
        var message = connectMessage(null);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectConnectWithInvalidToken() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        var message = connectMessage("bad");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldAuthenticateConnectWithValidToken() {
        var user = buildUser("user@test.com", UserRole.CUSTOMER);
        when(jwtUtil.validateToken("good")).thenReturn(true);
        when(jwtUtil.getEmail("good")).thenReturn("user@test.com");
        when(jwtUtil.getTokenVersion("good")).thenReturn(0);
        when(userCacheService.getCachedUser("user@test.com", 0))
                .thenReturn(new UserResponse(1L, "Test", "user@test.com", null, UserRole.CUSTOMER, null));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        var result = interceptor.preSend(connectMessage("good"), channel);

        var accessor = StompHeaderAccessor.wrap(result);
        var auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
        assertThat(auth).isNotNull();
        assertThat(((CustomUserDetails) auth.getPrincipal()).getUsername()).isEqualTo("user@test.com");
    }

    @Test
    void shouldRejectSubscribeWithoutAuthenticatedUser() {
        var message = subscribeMessage("/topic/orders/1", null);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldRejectNewOrdersSubscribeForNonAdmin() {
        var message = subscribeMessage("/topic/orders/new", authFor("user@test.com", UserRole.CUSTOMER));

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowNewOrdersSubscribeForAdmin() {
        var message = subscribeMessage("/topic/orders/new", authFor("admin@test.com", UserRole.ADMIN));

        var result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldAllowOrderTopicSubscribeForOwner() {
        when(orderRepository.findOwnerEmailById(1L)).thenReturn(Optional.of("user@test.com"));
        var message = subscribeMessage("/topic/orders/1", authFor("user@test.com", UserRole.CUSTOMER));

        var result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldRejectOrderTopicSubscribeForOtherCustomer() {
        when(orderRepository.findOwnerEmailById(1L)).thenReturn(Optional.of("owner@test.com"));
        var message = subscribeMessage("/topic/orders/1", authFor("other@test.com", UserRole.CUSTOMER));

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldAllowOrderTopicSubscribeForCourierRegardlessOfOwnership() {
        var message = subscribeMessage("/topic/orders/1", authFor("courier@test.com", UserRole.COURIER));

        var result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }
}
