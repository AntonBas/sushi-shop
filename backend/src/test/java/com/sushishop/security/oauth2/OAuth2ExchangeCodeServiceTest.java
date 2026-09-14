package com.sushishop.security.oauth2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2ExchangeCodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OAuth2ExchangeCodeService exchangeCodeService;

    @Test
    void shouldIssueCodeAndStoreEmailWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String code = exchangeCodeService.issueCode("anton@example.com");

        assertThat(code).isNotBlank();
        verify(valueOperations).set(startsWith("oauth2:exchange:"), eq("anton@example.com"), any(Duration.class));
    }

    @Test
    void shouldConsumeValidCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(anyString())).thenReturn("anton@example.com");

        var result = exchangeCodeService.consume("some-code");

        assertThat(result).contains("anton@example.com");
    }

    @Test
    void shouldReturnEmptyForUnknownOrExpiredCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(anyString())).thenReturn(null);

        var result = exchangeCodeService.consume("unknown-code");

        assertThat(result).isEmpty();
    }
}
