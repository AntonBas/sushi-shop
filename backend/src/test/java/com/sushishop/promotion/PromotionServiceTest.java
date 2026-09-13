package com.sushishop.promotion;

import com.sushishop.product.Category;
import com.sushishop.product.Product;
import com.sushishop.product.ProductCacheService;
import com.sushishop.product.ProductRepository;
import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.request.UpdatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.SlugService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

    private static final Long PROMOTION_ID = 1L;
    private static final String PROMOTION_SLUG = "weekend-sale";
    private static final String PROMOTION_TITLE = "Weekend Sale";
    private static final BigDecimal DISCOUNT_PERCENT = new BigDecimal("20.00");
    private static final String PRODUCT_NAME = "Maki";
    private static final Long PRODUCT_ID = 1L;
    private static final boolean ACTIVE = true;
    private static final boolean IS_CURRENTLY_ACTIVE = true;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromotionValidator validator;

    @Mock
    private PromotionResponseAssembler assembler;

    @Mock
    private SlugService slugService;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private PromotionService promotionService;

    private Product createProduct() {
        var product = Product.builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .build();
        product.setProductImages(new ArrayList<>());
        return product;
    }

    private PromotionResponse createPromotionResponse() {
        return new PromotionResponse(
                PROMOTION_ID,
                PROMOTION_SLUG,
                PROMOTION_TITLE,
                null,
                DISCOUNT_PERCENT,
                null,
                null,
                ACTIVE,
                IS_CURRENTLY_ACTIVE,
                List.of()
        );
    }

    @Test
    public void shouldCreatePromotion() {
        var request = new CreatePromotionRequest(
                PROMOTION_TITLE,
                "20% off",
                DISCOUNT_PERCENT,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                List.of(PRODUCT_ID)
        );

        var product = createProduct();

        var promoEntity = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .description("20% off")
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(ACTIVE)
                .products(Set.of(product))
                .build();

        var mappedResponse = createPromotionResponse();

        when(productRepository.findAllById(List.of(PRODUCT_ID))).thenReturn(List.of(product));
        when(slugService.generateUniqueSlug(eq(PROMOTION_TITLE), any())).thenReturn(PROMOTION_SLUG);
        when(promotionRepository.save(any())).thenReturn(promoEntity);
        when(assembler.toResponse(any())).thenReturn(mappedResponse);

        var result = promotionService.create(request);

        assertThat(result.title()).isEqualTo(PROMOTION_TITLE);
        assertThat(result.slug()).isEqualTo(PROMOTION_SLUG);
        verify(validator).validateDates(request.startDate(), request.endDate());
        verify(validator).validateTitleUnique(PROMOTION_TITLE);
        verify(validator).validateProductsExist(List.of(PRODUCT_ID));
        verify(validator).validateProductsNotInOtherActivePromotions(List.of(PRODUCT_ID), null);
        verify(promotionRepository).save(any());
    }

    @Test
    public void shouldGetActivePromotions() {
        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(ACTIVE)
                .products(Set.of())
                .build();

        var mappedResponse = createPromotionResponse();

        when(promotionRepository.findActiveAt(any(LocalDateTime.class))).thenReturn(List.of(promotion));
        when(assembler.toResponseList(List.of(promotion))).thenReturn(List.of(mappedResponse));

        var result = promotionService.getActive();

        assertThat(result).hasSize(1);
        verify(promotionRepository).findActiveAt(any(LocalDateTime.class));
    }

    @Test
    public void shouldGetAllWithSearch() {
        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(ACTIVE)
                .products(Set.of())
                .build();

        var mappedResponse = createPromotionResponse();

        when(promotionRepository.findIdsBySearch(eq("week"), any()))
                .thenReturn(new PageImpl<>(List.of(PROMOTION_ID)));
        when(promotionRepository.findPromotionsByIds(List.of(PROMOTION_ID)))
                .thenReturn(List.of(promotion));
        when(assembler.toResponseList(List.of(promotion))).thenReturn(List.of(mappedResponse));

        var result = promotionService.getAll(Pageable.unpaged(), "week");

        assertThat(result.getContent()).hasSize(1);
        verify(promotionRepository).findIdsBySearch(eq("week"), any());
        verify(promotionRepository).findPromotionsByIds(List.of(PROMOTION_ID));
    }

    @Test
    public void shouldGetAllWithoutSearch() {
        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(ACTIVE)
                .products(Set.of())
                .build();

        var mappedResponse = createPromotionResponse();

        when(promotionRepository.findIds(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(PROMOTION_ID)));
        when(promotionRepository.findPromotionsByIds(List.of(PROMOTION_ID)))
                .thenReturn(List.of(promotion));
        when(assembler.toResponseList(List.of(promotion))).thenReturn(List.of(mappedResponse));

        var result = promotionService.getAll(Pageable.unpaged(), null);

        assertThat(result.getContent()).hasSize(1);
        verify(promotionRepository).findIds(any(Pageable.class));
        verify(promotionRepository).findPromotionsByIds(List.of(PROMOTION_ID));
    }

    @Test
    public void shouldGetById() {
        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(ACTIVE)
                .products(Set.of())
                .build();

        var mappedResponse = createPromotionResponse();

        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
        when(assembler.toResponse(any())).thenReturn(mappedResponse);

        var result = promotionService.getById(PROMOTION_ID);

        assertThat(result.id()).isEqualTo(PROMOTION_ID);
        assertThat(result.title()).isEqualTo(PROMOTION_TITLE);
        verify(assembler).toResponse(promotion);
    }

    @Test
    public void shouldGetBySlug() {
        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .active(ACTIVE)
                .products(Set.of())
                .build();

        var mappedResponse = createPromotionResponse();

        when(promotionRepository.findBySlug(PROMOTION_SLUG)).thenReturn(Optional.of(promotion));
        when(assembler.toResponse(any())).thenReturn(mappedResponse);

        var result = promotionService.getBySlug(PROMOTION_SLUG);

        assertThat(result.slug()).isEqualTo(PROMOTION_SLUG);
        verify(assembler).toResponse(promotion);
    }

    @Test
    public void shouldThrowWhenPromotionNotFound() {
        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.getById(PROMOTION_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldUpdatePromotion() {
        var request = new UpdatePromotionRequest(
                "Updated Sale",
                "Updated desc",
                new BigDecimal("30.00"),
                null,
                null,
                null,
                false
        );

        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .description("Old desc")
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(ACTIVE)
                .products(new HashSet<>())
                .build();

        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
        when(slugService.generateUniqueSlug(eq("Updated Sale"), any())).thenReturn("updated-sale");
        when(promotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assembler.toResponse(any())).thenAnswer(inv -> {
            var p = inv.getArgument(0, Promotion.class);
            return new PromotionResponse(
                    p.getId(),
                    p.getSlug(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getDiscountPercent(),
                    p.getStartDate(),
                    p.getEndDate(),
                    p.isActive(),
                    p.isCurrentlyActive(),
                    List.of()
            );
        });

        var result = promotionService.update(PROMOTION_ID, request);

        assertThat(result.title()).isEqualTo("Updated Sale");
        assertThat(result.active()).isFalse();
        verify(validator).validateTitleUnique("Updated Sale");
        verify(promotionRepository).save(promotion);
    }

    @Test
    public void shouldUpdatePartialDates() {
        var request = new UpdatePromotionRequest(
                null,
                null,
                null,
                LocalDateTime.now().plusDays(5),
                null,
                null,
                null
        );

        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .description("Old desc")
                .discountPercent(DISCOUNT_PERCENT)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .active(ACTIVE)
                .products(new HashSet<>())
                .build();

        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assembler.toResponse(any())).thenAnswer(inv -> {
            var p = inv.getArgument(0, Promotion.class);
            return new PromotionResponse(
                    p.getId(),
                    p.getSlug(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getDiscountPercent(),
                    p.getStartDate(),
                    p.getEndDate(),
                    p.isActive(),
                    p.isCurrentlyActive(),
                    List.of()
            );
        });

        var result = promotionService.update(PROMOTION_ID, request);

        assertThat(result.id()).isEqualTo(PROMOTION_ID);
        assertThat(result.startDate()).isEqualTo(request.startDate());
        verify(validator).validateDates(request.startDate(), promotion.getEndDate());
    }

    @Test
    public void shouldDeletePromotion() {
        var promotion = Promotion.builder()
                .id(PROMOTION_ID)
                .slug(PROMOTION_SLUG)
                .title(PROMOTION_TITLE)
                .products(new HashSet<>())
                .build();

        when(promotionRepository.findById(PROMOTION_ID)).thenReturn(Optional.of(promotion));

        promotionService.delete(PROMOTION_ID);

        verify(promotionRepository).save(promotion);
        verify(promotionRepository).delete(promotion);
    }
}