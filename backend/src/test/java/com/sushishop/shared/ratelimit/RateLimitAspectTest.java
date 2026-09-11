package com.sushishop.shared.ratelimit;

import com.sushishop.shared.exception.core.RateLimitExceededException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @RateLimit
    private void annotatedMethod() {
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private RateLimit rateLimitAnnotation() throws NoSuchMethodException {
        return getClass().getDeclaredMethod("annotatedMethod").getAnnotation(RateLimit.class);
    }

    @Test
    void shouldIgnoreSpoofedForwardedForHeaderAndUseRemoteAddr() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        rateLimitAspect.checkRateLimit(rateLimitAnnotation());

        verify(rateLimitService).tryConsume("10.0.0.1", 1, 5, 60);
    }

    @Test
    void shouldThrowWhenLimitExceeded() throws NoSuchMethodException {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(rateLimitAnnotation()))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
