package com.sushishop.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class WebhookIdempotencyService {

    private static final String KEY_PREFIX = "stripe:webhook:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public boolean markProcessed(String eventId) {
        var wasAbsent = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + eventId, "1", TTL);
        return Boolean.TRUE.equals(wasAbsent);
    }

    public void unmark(String eventId) {
        redisTemplate.delete(KEY_PREFIX + eventId);
    }
}
