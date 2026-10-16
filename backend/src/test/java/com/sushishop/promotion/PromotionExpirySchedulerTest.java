package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.product.cache.ProductCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionExpirySchedulerTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private PromotionCacheService promotionCacheService;

    @Spy
    private PromotionBoundaryTracker boundaryTracker = new PromotionBoundaryTracker();

    @InjectMocks
    private PromotionExpiryScheduler scheduler;

    @Test
    void shouldEvictCachesForPromotionThatJustStartedOrEnded() {
        var product = Product.builder().id(1L).slug("maki").build();
        var promotion = Promotion.builder().id(1L).slug("summer").products(Set.of(product)).build();

        when(promotionRepository.findStartingOrEndingBetween(any(), any())).thenReturn(List.of(promotion));

        scheduler.evictCacheOnPromotionBoundaries();

        verify(productCacheService).evict(1L, "maki");
        verify(promotionCacheService).evict(1L, "summer");
    }

    @Test
    void shouldDoNothingWhenNoPromotionsExpired() {
        when(promotionRepository.findStartingOrEndingBetween(any(), any())).thenReturn(List.of());

        scheduler.evictCacheOnPromotionBoundaries();

        verify(productCacheService, never()).evict(any(), any());
    }

    @Test
    void shouldStartNextWindowWhereThePreviousOneEnded() {
        when(promotionRepository.findStartingOrEndingBetween(any(), any())).thenReturn(List.of());

        scheduler.evictCacheOnPromotionBoundaries();

        var toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(promotionRepository).findStartingOrEndingBetween(any(), toCaptor.capture());
        var firstRunEnd = toCaptor.getValue();

        boundaryTracker.reset();
        scheduler.evictCacheOnPromotionBoundaries();

        var fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(promotionRepository, times(2)).findStartingOrEndingBetween(fromCaptor.capture(), any());
        assertThat(fromCaptor.getAllValues().get(1)).isEqualTo(firstRunEnd);
    }

    @Test
    void shouldSkipDatabaseUntilNextBoundary() {
        when(promotionRepository.findStartingOrEndingBetween(any(), any())).thenReturn(List.of());
        when(promotionRepository.findNextStartAfter(any())).thenReturn(LocalDateTime.now().plusDays(1));

        scheduler.evictCacheOnPromotionBoundaries();
        scheduler.evictCacheOnPromotionBoundaries();

        verify(promotionRepository, times(1)).findStartingOrEndingBetween(any(), any());
    }

    @Test
    void shouldQueryAgainAfterReset() {
        when(promotionRepository.findStartingOrEndingBetween(any(), any())).thenReturn(List.of());

        scheduler.evictCacheOnPromotionBoundaries();
        scheduler.evictCacheOnPromotionBoundaries();
        boundaryTracker.reset();
        scheduler.evictCacheOnPromotionBoundaries();

        verify(promotionRepository, times(2)).findStartingOrEndingBetween(any(), any());
    }

    @Test
    void shouldQueryWhenBoundaryReached() {
        when(promotionRepository.findStartingOrEndingBetween(any(), any())).thenReturn(List.of());
        when(promotionRepository.findNextEndAfter(any())).thenReturn(LocalDateTime.now().minusSeconds(1));

        scheduler.evictCacheOnPromotionBoundaries();
        scheduler.evictCacheOnPromotionBoundaries();

        verify(promotionRepository, times(2)).findStartingOrEndingBetween(any(), any());
    }
}
