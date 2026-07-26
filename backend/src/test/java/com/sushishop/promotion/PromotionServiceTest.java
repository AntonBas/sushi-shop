package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void shouldCreatePromotion() {
        var request = new CreatePromotionRequest("Weekend Sale", "20% off", new BigDecimal("20.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), List.of(1L));
        var product = Product.builder().id(1L).build();
        var promotion = new Promotion();
        var expected = new PromotionResponse(1L, "Weekend Sale", "20% off", new BigDecimal("20.00"),
                request.startDate(), request.endDate(), true, List.of());

        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(promotionRepository.save(any())).thenReturn(promotion);
        when(promotionMapper.toResponse(any())).thenReturn(expected);

        var result = promotionService.create(request);

        assertThat(result.title()).isEqualTo("Weekend Sale");
        verify(promotionRepository).save(any());
    }

    @Test
    void shouldGetActivePromotions() {
        var expected = new PromotionResponse(1L, "Weekend Sale", null, new BigDecimal("20.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), true, List.of());

        when(promotionRepository.findByStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(new Promotion()));
        when(promotionMapper.toResponse(any())).thenReturn(expected);

        var result = promotionService.getActive();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetById() {
        var expected = new PromotionResponse(1L, "Weekend Sale", null, new BigDecimal("20.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), true, List.of());

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(new Promotion()));
        when(promotionMapper.toResponse(any())).thenReturn(expected);

        var result = promotionService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Weekend Sale");
    }

    @Test
    void shouldDeletePromotion() {
        var promotion = new Promotion();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        promotionService.delete(1L);

        verify(promotionRepository).delete(promotion);
    }
}