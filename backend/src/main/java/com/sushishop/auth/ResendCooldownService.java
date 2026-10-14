package com.sushishop.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ResendCooldownService {

    private static final String KEY_PREFIX = "auth:resend-cooldown:";

    private final StringRedisTemplate redisTemplate;

    public long acquire(String email, Duration cooldown) {
        var acquired = redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + email, "1", cooldown);
        if (Boolean.TRUE.equals(acquired)) {
            return 0;
        }
        return Math.max(1, remaining(email));
    }

    public long remaining(String email) {
        var ttl = redisTemplate.getExpire(KEY_PREFIX + email, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
