package com.sushishop.shared.ratelimit;

import com.sushishop.shared.exception.core.RateLimitExceededException;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    private record EmailRequest(String email) {
    }

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private JoinPoint joinPoint;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @RateLimit
    private void annotatedMethod() {
    }

    @RateLimit(key = {"ip", "email"})
    private void ipAndEmailAnnotatedMethod() {
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private RateLimit rateLimitAnnotation() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("annotatedMethod").getAnnotation(RateLimit.class);
    }

    private RateLimit ipAndEmailRateLimitAnnotation() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("ipAndEmailAnnotatedMethod").getAnnotation(RateLimit.class);
    }

    @Test
    void shouldIgnoreSpoofedForwardedForHeaderAndUseRemoteAddr() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        rateLimitAspect.checkRateLimit(joinPoint, rateLimitAnnotation());

        verify(rateLimitService).tryConsume("10.0.0.1", 1, 5, 60);
    }

    @Test
    void shouldThrowWhenLimitExceeded() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, rateLimitAnnotation()))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void shouldLimitByBothIpAndEmailWhenBothKeysConfigured() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[]{new EmailRequest("Anton@Example.com")});
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        rateLimitAspect.checkRateLimit(joinPoint, ipAndEmailRateLimitAnnotation());

        verify(rateLimitService).tryConsume(eq("10.0.0.1"), anyInt(), anyInt(), anyInt());
        verify(rateLimitService).tryConsume(eq("email:anton@example.com"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void shouldThrowWhenEmailLimitExceededEvenIfIpLimitOk() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[]{new EmailRequest("anton@example.com")});
        when(rateLimitService.tryConsume(eq("10.0.0.1"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(rateLimitService.tryConsume(eq("email:anton@example.com"), anyInt(), anyInt(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, ipAndEmailRateLimitAnnotation()))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void shouldThrowWhenEmailKeyConfiguredButNoRequestArgumentHasEmail() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not a request"});
        when(rateLimitService.tryConsume(eq("10.0.0.1"), anyInt(), anyInt(), anyInt())).thenReturn(true);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, ipAndEmailRateLimitAnnotation()))
                .isInstanceOf(IllegalStateException.class);
    }
}
