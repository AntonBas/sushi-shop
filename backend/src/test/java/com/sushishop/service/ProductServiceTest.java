package com.sushishop.service;

import com.sushishop.domain.Product;
import com.sushishop.domain.ProductImage;
import com.sushishop.domain.enums.Category;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductResponse;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.ProductMapper;
import com.sushishop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        var request = new CreateProductRequest("Maki", "Desc", new BigDecimal("250.00"), Category.ROLL);
        var product = new Product();
        product.setPromotions(new ArrayList<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        var expected = new ProductResponse(1L, "Maki", "Desc", new BigDecimal("250.00"), null, null, null, "ROLL", List.of(), 0, null, true);

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        var result = productService.create(request, null);

        assertThat(result.name()).isEqualTo("Maki");
        verify(productRepository).save(product);
    }

    @Test
    void shouldGetById() {
        var product = new Product();
        product.setPromotions(new ArrayList<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        var expected = new ProductResponse(1L, "Maki", "Desc", new BigDecimal("250.00"), null, null, null, "ROLL", List.of(), 0, null, true);

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
    void shouldUpdateProduct() {
        var request = new UpdateProductRequest("Updated", null, null, null, null);
        var product = new Product();
        product.setPromotions(new ArrayList<>());
        product.setReviews(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        var expected = new ProductResponse(1L, "Updated", "Desc", new BigDecimal("250.00"), null, null, null, "ROLL", List.of(), 0, null, true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
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
}