package com.sushishop.promotion;

import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.shared.config.RedisConfig;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class PromotionCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer activePromotionsCacheCustomizer() {
        return builder -> builder.withCacheConfiguration("activePromotions",
                RedisConfig.listCacheConfig(PromotionResponse.class, Duration.ofMinutes(15)));
    }
}
