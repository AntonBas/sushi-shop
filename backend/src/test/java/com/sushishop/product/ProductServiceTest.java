package com.sushishop.product;

import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductResponse;
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
public class ProductServiceTest {

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
    public void shouldCreateProduct() {
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

        var result = productService.create(request, null);

        assertThat(result.name()).isEqualTo("Maki");
        verify(productImageService).addImagesToProduct(product, null);
        verify(productRepository).save(product);
    }

    @Test
    public void shouldGetById() {
        var product = createProduct();
        var expected = createResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expected);
        when(enrichmentService.getAverageRatings(anyList())).thenReturn(Map.of());
        when(enrichmentService.calculateDiscountedPrice(product)).thenReturn(null);
        when(enrichmentService.getDiscountPercent(product)).thenReturn(null);
        when(enrichmentService.getPromotionTitle(product)).thenReturn(null);

        var result = productService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    public void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldUpdateProduct() {
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

        var result = productService.update(1L, request);

        assertThat(result.name()).isEqualTo("Maki");
        verify(productMapper).updateEntity(request, product);
    }

    @Test
    public void shouldToggleAvailability() {
        var product = createProduct();
        product.setAvailable(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.toggleAvailability(1L);

        assertThat(product.isAvailable()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    public void shouldDeleteProduct() {
        var product = createProduct();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }
}