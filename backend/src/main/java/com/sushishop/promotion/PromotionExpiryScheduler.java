package com.sushishop.promotion;

import com.sushishop.product.cache.ProductCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PromotionExpiryScheduler {

    private static final long INTERVAL_MS = 60_000;

    private final PromotionRepository promotionRepository;
    private final ProductCacheService productCacheService;
    private final PromotionCacheService promotionCacheService;
    private final PromotionBoundaryTracker boundaryTracker;

    private volatile LocalDateTime lastRun = LocalDateTime.now();

    @Scheduled(fixedRate = INTERVAL_MS)
    public void evictCacheOnPromotionBoundaries() {
        var now = LocalDateTime.now();
        var boundary = boundaryTracker.nextBoundary();
        if (now.isBefore(boundary)) {
            return;
        }
        var from = lastRun;

        promotionRepository.findStartingOrEndingBetween(from, now).forEach(promotion -> {
            promotionCacheService.evict(promotion.getId(), promotion.getSlug());
            promotion.getProducts().forEach(product ->
                    productCacheService.evict(product.getId(), product.getSlug()));
        });

        lastRun = now;
        boundaryTracker.update(boundary, findNextBoundaryAfter(now));
    }

    private LocalDateTime findNextBoundaryAfter(LocalDateTime after) {
        return Stream.of(promotionRepository.findNextStartAfter(after), promotionRepository.findNextEndAfter(after))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.MAX);
    }
}
