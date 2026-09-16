package com.sushishop.shared.ratelimit;

import com.sushishop.shared.exception.core.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        for (String keyType : rateLimit.key()) {
            String key = resolveKey(keyType, joinPoint.getArgs());

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
            default -> keyType;
        };
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        return attributes.getRequest().getRemoteAddr();
    }

    private String extractEmail(Object[] args) {
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            try {
                Method emailAccessor = arg.getClass().getMethod("email");
                if (emailAccessor.getReturnType() != String.class) {
                    continue;
                }
                var email = (String) emailAccessor.invoke(arg);
                if (email != null && !email.isBlank()) {
                    return email.toLowerCase();
                }
            } catch (NoSuchMethodException e) {
                // argument has no email() accessor, try the next one
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to read email for rate limiting", e);
            }
        }
        throw new IllegalStateException("RateLimit key=email requires a request argument with an email() accessor");
    }
}