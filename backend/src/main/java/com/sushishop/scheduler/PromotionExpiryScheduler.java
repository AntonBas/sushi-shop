package com.sushishop.scheduler;

import com.sushishop.product.ProductCacheService;
import com.sushishop.promotion.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PromotionExpiryScheduler {

    private static final long INTERVAL_MS = 60_000;

    private final PromotionRepository promotionRepository;
    private final ProductCacheService productCacheService;

    @Scheduled(fixedRate = INTERVAL_MS)
    public void evictExpiredPromotionsCache() {
        var now = LocalDateTime.now();
        var from = now.minusNanos(INTERVAL_MS * 1_000_000);

        promotionRepository.findEndingBetween(from, now).forEach(promotion ->
                promotion.getProducts().forEach(product ->
                        productCacheService.evict(product.getId(), product.getSlug())));
    }
}
