package com.sushishop.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResendCooldownServiceTest {

    private static final String KEY = "auth:resend-cooldown:user@example.com";
    private static final Duration COOLDOWN = Duration.ofSeconds(60);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ResendCooldownService resendCooldownService;

    @Test
    void shouldAllowFirstRequest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, "1", COOLDOWN)).thenReturn(true);

        assertThat(resendCooldownService.acquire("user@example.com", COOLDOWN)).isZero();
    }

    @Test
    void shouldReturnRemainingSecondsForRepeatedRequest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, "1", COOLDOWN)).thenReturn(false);
        when(redisTemplate.getExpire(KEY, TimeUnit.SECONDS)).thenReturn(42L);

        assertThat(resendCooldownService.acquire("user@example.com", COOLDOWN)).isEqualTo(42);
    }

    @Test
    void shouldReturnAtLeastOneSecondWhenKeyExpiresDuringCheck() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(KEY, "1", COOLDOWN)).thenReturn(false);
        when(redisTemplate.getExpire(KEY, TimeUnit.SECONDS)).thenReturn(-2L);

        assertThat(resendCooldownService.acquire("user@example.com", COOLDOWN)).isEqualTo(1);
    }
}
