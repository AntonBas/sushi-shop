package com.sushishop.product;

import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.shared.exception.core.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEnrichmentService enrichmentService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductQueryService productQueryService;

    private Product createProduct(Long id, String name) {
        var product = Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .weight(250)
                .pieces(8)
                .build();
        product.setProductImages(new ArrayList<>());
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());
        return product;
    }

    private ProductListResponse createListResponse(Long id, String name, Double rating) {
        return new ProductListResponse(
                id,
                "slug-" + id,
                name,
                new BigDecimal("250.00"),
                null,
                rating,
                Category.ROLL,
                null,
                true,
                250,
                8
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetAllProducts() {
        var product = createProduct(1L, "Maki");
        var pageable = PageRequest.of(0, 12);
        var page = new PageImpl<>(List.of(product), pageable, 1);
        var listResponse = createListResponse(1L, "Maki", 4.5);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(1L, 4.5));
        when(enrichmentService.calculateDiscountedPrice(product)).thenReturn(null);
        when(productMapper.toListResponse(eq(product), eq(4.5), any())).thenReturn(listResponse);

        var result = productQueryService.getAll(pageable, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Maki");
        verify(enrichmentService).enrichProductsWithImagesAndPromotions(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnEmptyPageWhenNoProducts() {
        var pageable = PageRequest.of(0, 12);
        var page = new PageImpl<Product>(List.of(), pageable, 0);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        var result = productQueryService.getAll(pageable, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSortProductsByRatingDescending() {
        var lowRated = createProduct(1L, "Low");
        var highRated = createProduct(2L, "High");
        var noReviews = createProduct(3L, "NoReviews");
        var pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "rating"));

        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(lowRated, highRated, noReviews));
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(1L, 3.0, 2L, 4.8));
        when(enrichmentService.calculateDiscountedPrice(any())).thenReturn(null);
        when(productMapper.toListResponse(eq(highRated), eq(4.8), any())).thenReturn(createListResponse(2L, "High", 4.8));
        when(productMapper.toListResponse(eq(lowRated), eq(3.0), any())).thenReturn(createListResponse(1L, "Low", 3.0));
        when(productMapper.toListResponse(eq(noReviews), isNull(), any())).thenReturn(createListResponse(3L, "NoReviews", null));

        var result = productQueryService.getAll(pageable, null, null, null);

        assertThat(result.getContent()).extracting(ProductListResponse::name)
                .containsExactly("High", "Low", "NoReviews");
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    public void shouldGetPopularProducts() {
        var product = createProduct(1L, "Maki");
        var listResponse = createListResponse(1L, "Maki", 4.5);

        when(productRepository.findPopular(any(Pageable.class))).thenReturn(List.of(product));
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(1L, 4.5));
        when(enrichmentService.calculateDiscountedPrice(product)).thenReturn(null);
        when(productMapper.toListResponse(eq(product), eq(4.5), any())).thenReturn(listResponse);

        var result = productQueryService.getPopular();

        assertThat(result).hasSize(1);
        verify(enrichmentService).enrichProductsWithImagesAndPromotions(anyList());
    }

    @Test
    public void shouldReturnEmptyListForPopularWhenNoProducts() {
        when(productRepository.findPopular(any(Pageable.class))).thenReturn(List.of());

        var result = productQueryService.getPopular();

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldGetRelatedProducts() {
        var product = createProduct(1L, "Maki");
        var relatedProduct = createProduct(2L, "Related");
        var listResponse = createListResponse(2L, "Related", 3.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findRelated(eq(Category.ROLL), eq(1L), any())).thenReturn(List.of(relatedProduct));
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(2L, 3.0));
        when(enrichmentService.calculateDiscountedPrice(relatedProduct)).thenReturn(null);
        when(productMapper.toListResponse(eq(relatedProduct), eq(3.0), any())).thenReturn(listResponse);

        var result = productQueryService.getRelated(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Related");
        verify(enrichmentService).enrichProductsWithImagesAndPromotions(anyList());
    }

    @Test
    public void shouldThrowWhenRelatedProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productQueryService.getRelated(1L))
                .isInstanceOf(NotFoundException.class);
    }
}