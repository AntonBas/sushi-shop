package com.sushishop.scheduler;

import com.sushishop.product.Product;
import com.sushishop.product.ProductCacheService;
import com.sushishop.promotion.Promotion;
import com.sushishop.promotion.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionExpirySchedulerTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductCacheService productCacheService;

    @InjectMocks
    private PromotionExpiryScheduler scheduler;

    @Test
    void shouldEvictCacheForProductsOfJustExpiredPromotion() {
        var product = Product.builder().id(1L).slug("maki").build();
        var promotion = Promotion.builder().id(1L).products(Set.of(product)).build();

        when(promotionRepository.findEndingBetween(any(), any())).thenReturn(List.of(promotion));

        scheduler.evictExpiredPromotionsCache();

        verify(productCacheService).evict(1L, "maki");
    }

    @Test
    void shouldDoNothingWhenNoPromotionsExpired() {
        when(promotionRepository.findEndingBetween(any(), any())).thenReturn(List.of());

        scheduler.evictExpiredPromotionsCache();

        verify(productCacheService, never()).evict(any(), any());
    }
}
