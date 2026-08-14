package com.sushishop.scheduler;

import com.sushishop.shared.ratelimit.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitCleanupScheduler {

    private final RateLimitService rateLimitService;

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredBuckets() {
        rateLimitService.cleanupBuckets();
    }
}