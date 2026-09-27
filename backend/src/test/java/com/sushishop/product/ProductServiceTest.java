package com.sushishop.product;

import com.sushishop.file.FileStorageService;
import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductResponse;
import com.sushishop.promotion.Promotion;
import com.sushishop.promotion.PromotionCacheService;
import com.sushishop.review.ReviewRepository;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.SlugService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SlugService slugService;

    @Mock
    private ProductEnrichmentService enrichmentService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PromotionCacheService promotionCacheService;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    private Product createProduct() {
        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .slug("maki")
                .description("Desc")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .weight(250)
                .pieces(8)
                .build();
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        return product;
    }

    private ProductResponse createResponse() {
        return new ProductResponse(
                1L, "maki", "Maki", "Desc",
                new BigDecimal("250.00"), null, null, null,
                Category.ROLL, List.of(), 0, null, true, 250, 8
        );
    }

    @Test
    void shouldCreateProduct() {
        var request = new CreateProductRequest("Maki", "Desc", new BigDecimal("250.00"), Category.ROLL, 250, 8);
        var product = createProduct();
        var expected = createResponse();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(slugService.generateUniqueSlug(eq("Maki"), any())).thenReturn("maki");
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of());
        when(enrichmentService.calculateDiscountedPrice(product)).thenReturn(null);
        when(enrichmentService.getDiscountPercent(product)).thenReturn(null);
        when(enrichmentService.getPromotionTitle(product)).thenReturn(null);
        when(reviewRepository.countByProductId(1L)).thenReturn(0L);

        var result = productService.create(request, null);

        assertThat(result.name()).isEqualTo("Maki");
        verify(productImageService).addImagesToProduct(product, null);
        verify(productRepository).save(product);
    }

    @Test
    void shouldGetById() {
        var product = createProduct();
        var expected = createResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expected);
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of());
        when(enrichmentService.calculateDiscountedPrice(product)).thenReturn(null);
        when(enrichmentService.getDiscountPercent(product)).thenReturn(null);
        when(enrichmentService.getPromotionTitle(product)).thenReturn(null);
        when(reviewRepository.countByProductId(1L)).thenReturn(0L);

        var result = productService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateProduct() {
        var request = new UpdateProductRequest("Updated", null, null, null, null, null);
        var product = createProduct();
        var expected = createResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(slugService.generateUniqueSlug(eq("Updated"), any())).thenReturn("updated");
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of());
        when(enrichmentService.calculateDiscountedPrice(product)).thenReturn(null);
        when(enrichmentService.getDiscountPercent(product)).thenReturn(null);
        when(enrichmentService.getPromotionTitle(product)).thenReturn(null);
        when(reviewRepository.countByProductId(1L)).thenReturn(0L);

        var result = productService.update(1L, request);

        assertThat(result.name()).isEqualTo("Maki");
        verify(productMapper).updateEntity(request, product);
    }

    @Test
    void shouldToggleAvailability() {
        var product = createProduct();
        product.setAvailable(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.toggleAvailability(1L);

        assertThat(product.isAvailable()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void shouldDeleteProductAndItsImageFiles() {
        var product = createProduct();
        product.getProductImages().add(ProductImage.builder().id(5L).url("/api/files/a.jpg").product(product).build());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.isReferencedByOrders(1L)).thenReturn(false);

        productService.delete(1L);

        verify(productRepository).delete(product);
        verify(fileStorageService).delete("/api/files/a.jpg");
    }

    @Test
    void shouldRejectDeletingOrderedProductWithoutTouchingFiles() {
        var product = createProduct();
        product.getProductImages().add(ProductImage.builder().id(5L).url("/api/files/a.jpg").product(product).build());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.isReferencedByOrders(1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.delete(1L)).isInstanceOf(ConflictException.class);

        verify(productRepository, never()).delete(any());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldRejectUpdateThatLeavesSetWithoutPieces() {
        var request = new UpdateProductRequest(null, null, null, Category.SET, null, null);
        var product = createProduct();
        product.setPieces(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doAnswer(inv -> {
            product.setCategory(Category.SET);
            return null;
        }).when(productMapper).updateEntity(request, product);

        assertThatThrownBy(() -> productService.update(1L, request)).isInstanceOf(BadRequestException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldEvictPromotionCacheWhenProductAvailabilityChanges() {
        var product = createProduct();
        product.getPromotions().add(Promotion.builder().id(7L).slug("summer").build());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.toggleAvailability(1L);

        verify(promotionCacheService).evict(7L, "summer");
    }
}