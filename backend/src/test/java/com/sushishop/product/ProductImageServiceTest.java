package com.sushishop.product;

import com.sushishop.file.FileStorageService;
import com.sushishop.shared.exception.core.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductImageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ProductCacheService productCacheService;

    @InjectMocks
    private ProductImageService productImageService;

    private Product createProduct() {
        var product = Product.builder().id(1L).name("Maki").build();
        product.setProductImages(new ArrayList<>());
        return product;
    }

    @Test
    public void shouldAddImage() {
        var product = createProduct();
        var file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test".getBytes());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(fileStorageService.store(file)).thenReturn("/api/files/test.jpg");

        productImageService.addImage(1L, file);

        assertThat(product.getProductImages()).hasSize(1);
        assertThat(product.getProductImages().getFirst().getSortOrder()).isZero();
        verify(productRepository).save(product);
    }

    @Test
    public void shouldSkipWhenFileIsEmpty() {
        var product = createProduct();
        var file = new MockMultipartFile("empty.jpg", "empty.jpg", "image/jpeg", new byte[0]);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(fileStorageService.store(file)).thenReturn(null);

        productImageService.addImage(1L, file);

        assertThat(product.getProductImages()).isEmpty();
        verify(productRepository, never()).save(product);
    }

    @Test
    public void shouldThrowWhenProductNotFoundForAddImage() {
        var file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test".getBytes());

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.addImage(1L, file)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldDeleteImage() {
        var product = createProduct();
        var image = ProductImage.builder().id(10L).url("/api/files/test.jpg").sortOrder(0).product(product).build();
        product.getProductImages().add(image);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productImageService.deleteImage(1L, 10L);

        assertThat(product.getProductImages()).isEmpty();
        verify(fileStorageService).delete("/api/files/test.jpg");
        verify(productRepository).save(product);
    }

    @Test
    public void shouldThrowWhenImageNotFound() {
        var product = createProduct();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productImageService.deleteImage(1L, 99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldReorderImages() {
        var product = createProduct();
        var image1 = ProductImage.builder().id(1L).url("1.jpg").sortOrder(0).product(product).build();
        var image2 = ProductImage.builder().id(2L).url("2.jpg").sortOrder(1).product(product).build();
        var image3 = ProductImage.builder().id(3L).url("3.jpg").sortOrder(2).product(product).build();
        product.getProductImages().addAll(List.of(image1, image2, image3));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productImageService.reorderImages(1L, List.of(3L, 1L, 2L));

        assertThat(image1.getSortOrder()).isEqualTo(1);
        assertThat(image2.getSortOrder()).isEqualTo(2);
        assertThat(image3.getSortOrder()).isZero();
        verify(productRepository).save(product);
    }

    @Test
    public void shouldAddImagesToProduct() {
        var product = createProduct();
        var file1 = new MockMultipartFile("1.jpg", "1.jpg", "image/jpeg", "1".getBytes());
        var file2 = new MockMultipartFile("2.jpg", "2.jpg", "image/jpeg", "2".getBytes());

        when(fileStorageService.store(file1)).thenReturn("/api/files/1.jpg");
        when(fileStorageService.store(file2)).thenReturn("/api/files/2.jpg");

        productImageService.addImagesToProduct(product, List.of(file1, file2));

        assertThat(product.getProductImages()).hasSize(2);
        assertThat(product.getProductImages().get(0).getSortOrder()).isZero();
        assertThat(product.getProductImages().get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    public void shouldSkipNullImagesInAddImagesToProduct() {
        var product = createProduct();

        productImageService.addImagesToProduct(product, null);

        assertThat(product.getProductImages()).isEmpty();
    }
}