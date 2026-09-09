package com.sushishop.shared.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {

    private record BucketEntry(Bucket bucket, long capacity) {
    }

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int tokens, int capacity, int durationInSeconds) {
        String bucketKey = key + ":" + capacity + ":" + durationInSeconds;

        BucketEntry entry = buckets.computeIfAbsent(bucketKey, k -> new BucketEntry(
                Bucket.builder()
                        .addLimit(limit -> limit.capacity(capacity)
                                .refillIntervally(capacity, Duration.ofSeconds(durationInSeconds)))
                        .build(),
                capacity));

        ConsumptionProbe probe = entry.bucket().tryConsumeAndReturnRemaining(tokens);
        return probe.isConsumed();
    }

    public void cleanupBuckets() {
        buckets.entrySet().removeIf(entry -> entry.getValue().bucket().getAvailableTokens() >= entry.getValue().capacity());
    }
}