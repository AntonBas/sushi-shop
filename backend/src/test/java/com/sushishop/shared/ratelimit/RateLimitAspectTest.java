package com.sushishop.shared.ratelimit;

import com.sushishop.shared.exception.core.RateLimitExceededException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

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

    @Mock
    private Signature signature;

    private RateLimitAspect rateLimitAspect;

    @RateLimit
    private void annotatedMethod() {
    }

    @RateLimit(key = {"ip", "email"})
    private void ipAndEmailAnnotatedMethod() {
    }

    @RateLimit(key = "user")
    private void userAnnotatedMethod() {
    }

    @BeforeEach
    void setUp() {
        rateLimitAspect = new RateLimitAspect(rateLimitService, "");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("AuthController.login(..)");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    private RateLimit rateLimitAnnotation() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("annotatedMethod").getAnnotation(RateLimit.class);
    }

    private RateLimit ipAndEmailRateLimitAnnotation() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("ipAndEmailAnnotatedMethod").getAnnotation(RateLimit.class);
    }

    private RateLimit userRateLimitAnnotation() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("userAnnotatedMethod").getAnnotation(RateLimit.class);
    }

    @Test
    void shouldLimitByAuthenticatedUserWhenUserKeyIsUsed() throws NoSuchMethodException {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("Anton@Example.com", null, List.of()));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        rateLimitAspect.checkRateLimit(joinPoint, userRateLimitAnnotation());

        verify(rateLimitService).tryConsume("AuthController.login(..):user:anton@example.com", 1, 5, 60);
    }

    @Test
    void shouldFallBackToClientIpForAnonymousUserKey() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        rateLimitAspect.checkRateLimit(joinPoint, userRateLimitAnnotation());

        verify(rateLimitService).tryConsume("AuthController.login(..):10.0.0.2", 1, 5, 60);
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

        verify(rateLimitService).tryConsume("AuthController.login(..):10.0.0.1", 1, 5, 60);
    }

    @Test
    void shouldUseConfiguredClientIpHeaderInsteadOfRemoteAddr() throws NoSuchMethodException {
        var aspect = new RateLimitAspect(rateLimitService, "CF-Connecting-IP");
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "6.6.6.6, 203.0.113.9");
        request.addHeader("CF-Connecting-IP", "203.0.113.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        aspect.checkRateLimit(joinPoint, rateLimitAnnotation());

        verify(rateLimitService).tryConsume("AuthController.login(..):203.0.113.9", 1, 5, 60);
    }

    @Test
    void shouldFallBackToRemoteAddrWhenConfiguredHeaderMissing() throws NoSuchMethodException {
        var aspect = new RateLimitAspect(rateLimitService, "CF-Connecting-IP");
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        aspect.checkRateLimit(joinPoint, rateLimitAnnotation());

        verify(rateLimitService).tryConsume("AuthController.login(..):10.0.0.1", 1, 5, 60);
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

        verify(rateLimitService).tryConsume(eq("AuthController.login(..):10.0.0.1"), anyInt(), anyInt(), anyInt());
        verify(rateLimitService).tryConsume(eq("AuthController.login(..):email:anton@example.com"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void shouldThrowWhenEmailLimitExceededEvenIfIpLimitOk() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[]{new EmailRequest("anton@example.com")});
        when(rateLimitService.tryConsume(eq("AuthController.login(..):10.0.0.1"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(rateLimitService.tryConsume(eq("AuthController.login(..):email:anton@example.com"), anyInt(), anyInt(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, ipAndEmailRateLimitAnnotation()))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void shouldThrowWhenEmailKeyConfiguredButNoRequestArgumentHasEmail() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not a request"});
        when(rateLimitService.tryConsume(eq("AuthController.login(..):10.0.0.1"), anyInt(), anyInt(), anyInt())).thenReturn(true);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, ipAndEmailRateLimitAnnotation()))
                .isInstanceOf(IllegalStateException.class);
    }
}
