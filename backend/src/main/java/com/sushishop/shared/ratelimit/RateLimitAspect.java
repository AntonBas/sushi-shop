package com.sushishop.shared.ratelimit;

import com.sushishop.shared.exception.core.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final String clientIpHeader;

    public RateLimitAspect(RateLimitService rateLimitService,
                           @Value("${app.rate-limit.client-ip-header:}") String clientIpHeader) {
        this.rateLimitService = rateLimitService;
        this.clientIpHeader = clientIpHeader;
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
            default -> keyType;
        };
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        if (!clientIpHeader.isBlank()) {
            String headerValue = request.getHeader(clientIpHeader);
            if (headerValue != null && !headerValue.isBlank()) {
                return headerValue.strip();
            }
        }
        return request.getRemoteAddr();
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