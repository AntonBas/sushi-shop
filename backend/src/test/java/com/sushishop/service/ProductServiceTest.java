package com.sushishop.service;

import com.sushishop.domain.Product;
import com.sushishop.domain.enums.Category;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductResponse;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.ProductMapper;
import com.sushishop.mapper.ReviewMapper;
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
    private ReviewMapper reviewMapper;

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
        var expected = new ProductResponse(1L, "Maki", "Desc", new BigDecimal("250.00"), null, null, null, "ROLL", List.of(), List.of(), true);

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
        var expected = new ProductResponse(1L, "Maki", "Desc", new BigDecimal("250.00"), null, null, null, "ROLL", List.of(), List.of(), true);

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
        var expected = new ProductResponse(1L, "Updated", "Desc", new BigDecimal("250.00"), null, null, null, "ROLL", List.of(), List.of(), true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expected);

        var result = productService.update(1L, request);

        assertThat(result.name()).isEqualTo("Updated");
        verify(productMapper).updateEntity(request, product);
    }

    @Test
    void shouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void shouldToggleAvailability() {
        var product = Product.builder().available(true).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.toggleAvailability(1L);

        verify(productRepository).save(product);
    }
}