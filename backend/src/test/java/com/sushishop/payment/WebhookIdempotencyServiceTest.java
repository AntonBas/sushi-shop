package com.sushishop.payment;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookIdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WebhookIdempotencyService webhookIdempotencyService;

    @Test
    void shouldReturnTrueForFirstOccurrenceOfEvent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("stripe:webhook:evt_123"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        assertThat(webhookIdempotencyService.markProcessed("evt_123")).isTrue();
    }

    @Test
    void shouldReturnFalseForDuplicateEvent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("stripe:webhook:evt_123"), eq("1"), any(Duration.class)))
                .thenReturn(false);

        assertThat(webhookIdempotencyService.markProcessed("evt_123")).isFalse();
    }
}
