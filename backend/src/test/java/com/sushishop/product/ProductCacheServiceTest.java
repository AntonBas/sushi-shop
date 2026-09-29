package com.sushishop.product;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductCacheServiceTest {

    private final CacheManager cacheManager = mock(CacheManager.class);
    private final Cache productsCache = mock(Cache.class);
    private final Cache popularProductsCache = mock(Cache.class);
    private final ProductCacheService productCacheService = new ProductCacheService(cacheManager);

    @Test
    void shouldEvictProductAndPopularList() {
        when(cacheManager.getCache("products")).thenReturn(productsCache);
        when(cacheManager.getCache("popularProducts")).thenReturn(popularProductsCache);

        productCacheService.evict(1L, "philadelphia");

        verify(productsCache).evict("id:1");
        verify(productsCache).evict("slug:philadelphia");
        verify(popularProductsCache).evict("all");
    }

    @Test
    void shouldEvictPopularList() {
        when(cacheManager.getCache("popularProducts")).thenReturn(popularProductsCache);

        productCacheService.evictPopular();

        verify(popularProductsCache).evict("all");
    }
}
