package com.sushishop.promotion;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromotionCacheServiceTest {

    private final CacheManager cacheManager = mock(CacheManager.class);
    private final Cache promotionsCache = mock(Cache.class);
    private final Cache activePromotionsCache = mock(Cache.class);
    private final PromotionCacheService promotionCacheService = new PromotionCacheService(cacheManager);

    @Test
    void shouldEvictPromotionAndActiveList() {
        when(cacheManager.getCache("promotions")).thenReturn(promotionsCache);
        when(cacheManager.getCache("activePromotions")).thenReturn(activePromotionsCache);

        promotionCacheService.evict(1L, "autumn-roll-fest");

        verify(promotionsCache).evict("id:1");
        verify(promotionsCache).evict("slug:autumn-roll-fest");
        verify(activePromotionsCache).evict("all");
    }

    @Test
    void shouldEvictActiveList() {
        when(cacheManager.getCache("activePromotions")).thenReturn(activePromotionsCache);

        promotionCacheService.evictActive();

        verify(activePromotionsCache).evict("all");
    }
}
