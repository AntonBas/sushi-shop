package com.sushishop.product;

import com.sushishop.file.FileStorageService;
import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import com.sushishop.shared.enums.Category;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.SlugService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
    private FileStorageService fileStorageService;

    @Mock
    private SlugService slugService;

    @Mock
    private ProductEnrichmentService productEnrichmentService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        var request = new CreateProductRequest("Maki", "Desc", new BigDecimal("250.00"), Category.ROLL, 250, 8);
        var product = new Product();
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        var expected = new ProductResponse(1L, "maki", "Maki", "Desc", new BigDecimal("250.00"), null, null, null, Category.ROLL, List.of(), 0, null, true, 250, 8);

        when(productMapper.toEntity(request)).thenReturn(product);
        when(slugService.generateUniqueSlug(eq("Maki"), any())).thenReturn("maki");
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        var result = productService.create(request, null);

        assertThat(result.name()).isEqualTo("Maki");
        verify(productRepository).save(product);
    }

    @Test
    void shouldGetById() {
        var product = new Product();
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        var expected = new ProductResponse(1L, "maki", "Maki", "Desc", new BigDecimal("250.00"), null, null, null, Category.ROLL, List.of(), 0, null, true, 250, 8);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expected);

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
    @SuppressWarnings("unchecked")
    void shouldGetAllProducts() {
        Product product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00"))
                .category(Category.ROLL).available(true).weight(250).pieces(8).build();
        product.setProductImages(new ArrayList<>());
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());

        Pageable pageable = PageRequest.of(0, 12);
        var page = new PageImpl<>(List.of(product), pageable, 1);
        var listResponse = new ProductListResponse(1L, "maki", "Maki", new BigDecimal("250.00"), null, 4.5, Category.ROLL, null, true, 250, 8);

        doNothing().when(productEnrichmentService).enrichProductsWithImagesAndPromotions(anyList());
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(productEnrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(1L, 4.5));
        when(productMapper.toListResponse(product, 4.5)).thenReturn(listResponse);

        var result = productService.getAll(pageable, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Maki");
        verify(productEnrichmentService).enrichProductsWithImagesAndPromotions(anyList());
    }

    @Test
    void shouldGetPopularProducts() {
        Product product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00"))
                .category(Category.ROLL).available(true).weight(250).pieces(8).build();
        product.setProductImages(new ArrayList<>());
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());

        var listResponse = new ProductListResponse(1L, "maki", "Maki", new BigDecimal("250.00"), null, 4.5, Category.ROLL, null, true, 250, 8);

        doNothing().when(productEnrichmentService).enrichProductsWithImagesAndPromotions(anyList());
        when(productRepository.findPopular(any(Pageable.class))).thenReturn(List.of(product));
        when(productEnrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(1L, 4.5));
        when(productMapper.toListResponse(product, 4.5)).thenReturn(listResponse);

        var result = productService.getPopular();

        assertThat(result).hasSize(1);
        verify(productEnrichmentService).enrichProductsWithImagesAndPromotions(anyList());
    }

    @Test
    void shouldUpdateProduct() {
        var request = new UpdateProductRequest("Updated", null, null, null, null, null);
        var product = new Product();
        product.setName("Old Name");
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        var expected = new ProductResponse(1L, "updated", "Updated", "Desc", new BigDecimal("250.00"), null, null, null, Category.ROLL, List.of(), 0, null, true, null, null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(slugService.generateUniqueSlug(eq("Updated"), any())).thenReturn("updated");
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        var result = productService.update(1L, request);

        assertThat(result.name()).isEqualTo("Updated");
        verify(productMapper).updateEntity(request, product);
    }

    @Test
    void shouldDeleteProduct() {
        var product = new Product();
        product.setProductImages(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldDeleteProductWithImages() {
        var product = new Product();
        var image = new ProductImage();
        image.setUrl("/api/files/test.jpg");
        product.setProductImages(new ArrayList<>(List.of(image)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(fileStorageService).delete("/api/files/test.jpg");
        verify(productRepository).delete(product);
    }

    @Test
    void shouldDeleteImage() {
        var product = new Product();
        var image = new ProductImage();
        image.setId(10L);
        image.setUrl("/api/files/test.jpg");
        product.setProductImages(new ArrayList<>(List.of(image)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteImage(1L, 10L);

        verify(fileStorageService).delete("/api/files/test.jpg");
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenDeleteImageNotFound() {
        var product = new Product();
        product.setProductImages(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteImage(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldToggleAvailability() {
        var product = new Product();
        product.setAvailable(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.toggleAvailability(1L);

        assertThat(product.isAvailable()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void shouldGetRelatedProducts() {
        var product = Product.builder().id(1L).category(Category.ROLL).build();
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());

        var relatedProduct = Product.builder().id(2L).name("Related").price(BigDecimal.TEN)
                .category(Category.ROLL).available(true).build();
        relatedProduct.setPromotions(new HashSet<>());
        relatedProduct.setReviews(new ArrayList<>());
        relatedProduct.setProductImages(new ArrayList<>());

        var listResponse = new ProductListResponse(2L, "related", "Related", BigDecimal.TEN, null, 3.0, Category.ROLL, null, true, null, null);

        doNothing().when(productEnrichmentService).enrichProductsWithImagesAndPromotions(anyList());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findRelated(Category.ROLL, 1L)).thenReturn(List.of(relatedProduct));
        when(productEnrichmentService.getAverageRatings(anyList())).thenReturn(Map.of(2L, 3.0));
        when(productMapper.toListResponse(relatedProduct, 3.0)).thenReturn(listResponse);

        var result = productService.getRelated(1L);

        assertThat(result).hasSize(1);
        verify(productEnrichmentService).enrichProductsWithImagesAndPromotions(anyList());
    }
}