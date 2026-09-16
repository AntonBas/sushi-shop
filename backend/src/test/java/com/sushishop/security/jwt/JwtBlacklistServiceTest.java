package com.sushishop.security.jwt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtBlacklistService jwtBlacklistService;

    @Test
    void shouldStoreJtiWithRemainingTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        jwtBlacklistService.blacklist("jti-123", Duration.ofSeconds(30));

        verify(valueOperations).set(eq("jwt:blacklist:jti-123"), eq("1"), eq(Duration.ofSeconds(30)));
    }

    @Test
    void shouldNotStoreWhenTtlIsZeroOrNegative() {
        jwtBlacklistService.blacklist("jti-123", Duration.ZERO);
        jwtBlacklistService.blacklist("jti-123", Duration.ofSeconds(-5));

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldNotStoreWhenJtiIsNull() {
        jwtBlacklistService.blacklist(null, Duration.ofSeconds(30));

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldReportBlacklistedWhenKeyExists() {
        when(redisTemplate.hasKey("jwt:blacklist:jti-123")).thenReturn(true);

        assertThat(jwtBlacklistService.isBlacklisted("jti-123")).isTrue();
    }

    @Test
    void shouldReportNotBlacklistedWhenKeyMissing() {
        when(redisTemplate.hasKey("jwt:blacklist:jti-123")).thenReturn(false);

        assertThat(jwtBlacklistService.isBlacklisted("jti-123")).isFalse();
    }

    @Test
    void shouldReportNotBlacklistedForNullJti() {
        assertThat(jwtBlacklistService.isBlacklisted(null)).isFalse();
        verify(redisTemplate, never()).hasKey(anyString());
    }
}
