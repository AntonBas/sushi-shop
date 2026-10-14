package com.sushishop.shared.ratelimit;

import com.sushishop.shared.exception.core.RateLimitExceededException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final ClientIpResolver clientIpResolver;

    public RateLimitAspect(RateLimitService rateLimitService, ClientIpResolver clientIpResolver) {
        this.rateLimitService = rateLimitService;
        this.clientIpResolver = clientIpResolver;
    }

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        String endpoint = joinPoint.getSignature().toShortString();
        for (String keyType : rateLimit.key()) {
            String key = endpoint + ":" + resolveKey(keyType, joinPoint.getArgs());

            boolean allowed = rateLimitService.tryConsume(
                    key,
                    1,
                    rateLimit.value(),
                    rateLimit.duration()
            );

            if (!allowed) {
                throw new RateLimitExceededException();
            }
        }
    }

    private String resolveKey(String keyType, Object[] args) {
        return switch (keyType) {
            case "ip" -> getClientIp();
            case "email" -> "email:" + extractEmail(args);
            case "user" -> resolveUser();
            default -> keyType;
        };
    }

    private String resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return getClientIp();
        }
        return "user:" + authentication.getName().toLowerCase();
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        return clientIpResolver.resolve(attributes.getRequest());
    }

    private String extractEmail(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            var emailAccessor = findEmailAccessor(arg.getClass());
            if (emailAccessor.isEmpty()) {
                continue;
            }
            try {
                var email = (String) emailAccessor.get().invoke(arg);
                if (email != null && !email.isBlank()) {
                    return email.toLowerCase();
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to read email for rate limiting", e);
            }
        }
        throw new IllegalStateException("RateLimit key=email requires a request argument with an email() accessor");
    }

    private Optional<Method> findEmailAccessor(Class<?> type) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().equals("email")
                        && method.getParameterCount() == 0
                        && method.getReturnType() == String.class)
                .findFirst();
    }
}
