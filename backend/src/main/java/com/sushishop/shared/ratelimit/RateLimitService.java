package com.sushishop.shared.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int tokens, int capacity, int durationInSeconds) {
        String bucketKey = key + ":" + capacity + ":" + durationInSeconds;

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity)
                        .refillIntervally(capacity, Duration.ofSeconds(durationInSeconds)))
                .build());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(tokens);
        return probe.isConsumed();
    }

    public void cleanupBuckets() {
        buckets.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            long availableTokens = bucket.getAvailableTokens();
            return availableTokens > 0;
        });
    }
}