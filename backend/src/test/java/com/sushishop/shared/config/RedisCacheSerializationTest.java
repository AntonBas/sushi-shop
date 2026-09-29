package com.sushishop.shared.config;

import com.sushishop.product.Category;
import com.sushishop.product.ProductCacheConfig;
import com.sushishop.product.dto.response.ProductImageResponse;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import com.sushishop.promotion.PromotionCacheConfig;
import com.sushishop.promotion.dto.response.PromotionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCacheSerializationTest {

    private final Map<String, RedisCacheConfiguration> configurations = initializedConfigurations();

    private final ProductListResponse product = new ProductListResponse(1L, "philadelphia", "Philadelphia",
            new BigDecimal("189.00"), new BigDecimal("151.20"), 4.5, Category.ROLL, "/img.webp", true, 250, 8);

    private final PromotionResponse promotion = new PromotionResponse(1L, "autumn-roll-fest", "Autumn Roll Fest",
            "Fresh rolls", new BigDecimal("20.00"), LocalDateTime.of(2026, 10, 1, 0, 0),
            LocalDateTime.of(2026, 10, 20, 23, 59), true, true, List.of(product));

    @Test
    void shouldRoundTripPopularProducts() {
        var products = List.of(product, product);

        assertThat(roundTrip("popularProducts", products)).isEqualTo(products);
    }

    @Test
    void shouldRoundTripActivePromotions() {
        var promotions = List.of(promotion);

        assertThat(roundTrip("activePromotions", promotions)).isEqualTo(promotions);
    }

    @Test
    void shouldRoundTripEmptyActivePromotions() {
        assertThat(roundTrip("activePromotions", List.of())).isEqualTo(List.of());
    }

    @Test
    void shouldRoundTripSinglePromotion() {
        assertThat(roundTrip("promotions", promotion)).isEqualTo(promotion);
    }

    @Test
    void shouldRoundTripSingleProduct() {
        var response = new ProductResponse(1L, "philadelphia", "Philadelphia", "Classic roll",
                new BigDecimal("189.00"), null, null, null, Category.ROLL,
                List.of(new ProductImageResponse(1L, "/img.webp")), 3, 4.5, true, 250, 8);

        assertThat(roundTrip("products", response)).isEqualTo(response);
    }

    private static Map<String, RedisCacheConfiguration> initializedConfigurations() {
        var customizers = new StaticListableBeanFactory(Map.of(
                "popularProducts", new ProductCacheConfig().popularProductsCacheCustomizer(),
                "activePromotions", new PromotionCacheConfig().activePromotionsCacheCustomizer()
        )).getBeanProvider(RedisCacheManagerBuilderCustomizer.class);
        var cacheManager = new RedisConfig().cacheManager(Mockito.mock(RedisConnectionFactory.class), customizers);
        cacheManager.afterPropertiesSet();
        return cacheManager.getCacheConfigurations();
    }

    private Object roundTrip(String cacheName, Object value) {
        var pair = configurations.get(cacheName).getValueSerializationPair();
        return pair.read(pair.write(value));
    }
}
