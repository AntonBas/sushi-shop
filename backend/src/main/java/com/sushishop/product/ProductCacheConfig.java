package com.sushishop.product;

import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.shared.config.RedisConfig;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ProductCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer popularProductsCacheCustomizer() {
        return builder -> builder.withCacheConfiguration("popularProducts",
                RedisConfig.listCacheConfig(ProductListResponse.class, Duration.ofMinutes(10)));
    }
}
