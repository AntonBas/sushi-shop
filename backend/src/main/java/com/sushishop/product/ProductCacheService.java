package com.sushishop.product;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCacheService {

    private final CacheManager cacheManager;

    public void evict(Long id, String slug) {
        var cache = cacheManager.getCache("products");
        if (cache == null) {
            return;
        }
        cache.evict("id:" + id);
        cache.evict("slug:" + slug);
    }
}
