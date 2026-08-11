package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.product.ProductEnrichmentService;
import com.sushishop.product.ProductMapper;
import com.sushishop.product.ProductRepository;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.shared.enums.Category;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.SlugService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductEnrichmentService productEnrichmentService;

    @Mock
    private SlugService slugService;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void shouldCreatePromotion() {
        var request = new CreatePromotionRequest("Weekend Sale", "20% off", new BigDecimal("20.00"),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7), List.of(1L));
        var product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00")).category(Category.ROLL).available(true).build();
        product.setProductImages(new ArrayList<>());

        var promoEntity = Promotion.builder()
                .id(1L)
                .slug("weekend-sale")
                .title("Weekend Sale")
                .description("20% off")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(true)
                .products(Set.of(product))
                .build();

        var mappedResponse = new PromotionResponse(1L, "weekend-sale", "Weekend Sale", "20% off", new BigDecimal("20.00"),
                request.startDate(), request.endDate(), true, List.of());

        when(promotionRepository.findByTitle("Weekend Sale")).thenReturn(Optional.empty());
        when(promotionRepository.findByStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of());
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));
        when(slugService.generateUniqueSlug(eq("Weekend Sale"), any())).thenReturn("weekend-sale");
        when(promotionRepository.save(any())).thenReturn(promoEntity);
        when(promotionMapper.toResponse(any())).thenReturn(mappedResponse);
        when(productEnrichmentService.getAverageRatings(anyList())).thenReturn(Map.of());
        when(productMapper.toListResponse(any(), any(), any())).thenReturn(new ProductListResponse(1L, "maki", "Maki", new BigDecimal("250.00"), new BigDecimal("200.00"), null, Category.ROLL, null, true, null, null));

        var result = promotionService.create(request);

        assertThat(result.title()).isEqualTo("Weekend Sale");
        assertThat(result.slug()).isEqualTo("weekend-sale");
        verify(promotionRepository).save(any());
    }

    @Test
    void shouldThrowWhenProductInAnotherActivePromotion() {
        var request = new CreatePromotionRequest("New Sale", "Desc", new BigDecimal("20.00"),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7), List.of(1L));

        var existingPromotion = Promotion.builder()
                .id(1L)
                .title("Weekend Sale")
                .products(Set.of(Product.builder().id(1L).name("Maki").build()))
                .build();

        when(promotionRepository.findByTitle("New Sale")).thenReturn(Optional.empty());
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(Product.builder().id(1L).name("Maki").build()));
        when(promotionRepository.findByStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(existingPromotion));

        assertThatThrownBy(() -> promotionService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in active promotion");
    }

    @Test
    void shouldGetActivePromotions() {
        var product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00")).category(Category.ROLL).available(true).build();
        product.setProductImages(new ArrayList<>());

        var promotion = Promotion.builder()
                .id(1L)
                .slug("weekend-sale")
                .title("Weekend Sale")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .products(Set.of(product))
                .build();

        var mappedResponse = new PromotionResponse(1L, "weekend-sale", "Weekend Sale", null, new BigDecimal("20.00"),
                null, null, true, List.of());

        when(promotionRepository.findByStartDateBeforeAndEndDateAfter(any(), any())).thenReturn(List.of(promotion));
        when(promotionMapper.toResponse(any())).thenReturn(mappedResponse);
        when(productEnrichmentService.getAverageRatings(anyList())).thenReturn(Map.of());
        when(productMapper.toListResponse(any(), any(), any())).thenReturn(new ProductListResponse(1L, "maki", "Maki", new BigDecimal("250.00"), new BigDecimal("200.00"), null, Category.ROLL, null, true, null, null));

        var result = promotionService.getActive();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetById() {
        var promotion = Promotion.builder()
                .id(1L)
                .slug("weekend-sale")
                .title("Weekend Sale")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .products(Set.of())
                .build();

        var mappedResponse = new PromotionResponse(1L, "weekend-sale", "Weekend Sale", null, new BigDecimal("20.00"),
                null, null, true, List.of());

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionMapper.toResponse(any())).thenReturn(mappedResponse);

        var result = promotionService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Weekend Sale");
    }

    @Test
    void shouldGetBySlug() {
        var promotion = Promotion.builder()
                .id(1L)
                .slug("weekend-sale")
                .title("Weekend Sale")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .products(Set.of())
                .build();

        var mappedResponse = new PromotionResponse(1L, "weekend-sale", "Weekend Sale", null, new BigDecimal("20.00"),
                null, null, true, List.of());

        when(promotionRepository.findBySlug("weekend-sale")).thenReturn(Optional.of(promotion));
        when(promotionMapper.toResponse(any())).thenReturn(mappedResponse);

        var result = promotionService.getBySlug("weekend-sale");

        assertThat(result.slug()).isEqualTo("weekend-sale");
    }

    @Test
    void shouldThrowWhenPromotionNotFound() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenStartDateAfterEndDate() {
        var request = new CreatePromotionRequest("Test", "Desc", new BigDecimal("20.00"),
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), List.of());

        assertThatThrownBy(() -> promotionService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowWhenEndDateInPast() {
        var request = new CreatePromotionRequest("Test", "Desc", new BigDecimal("20.00"),
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), List.of());

        assertThatThrownBy(() -> promotionService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldDeletePromotion() {
        var promotion = new Promotion();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));

        promotionService.delete(1L);

        verify(promotionRepository).delete(promotion);
    }
}