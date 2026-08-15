package com.sushishop.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ProductImageMapperTest {

    @Autowired
    private ProductImageMapper productImageMapper;

    @Test
    public void shouldReturnMainImageWithLowestSortOrder() {
        var image1 = ProductImage.builder()
                .id(1L)
                .url("/api/files/1.jpg")
                .sortOrder(2)
                .build();

        var image2 = ProductImage.builder()
                .id(2L)
                .url("/api/files/2.jpg")
                .sortOrder(1)
                .build();

        var result = productImageMapper.getMainImage(List.of(image1, image2));

        assertThat(result).isEqualTo("/api/files/2.jpg");
    }

    @Test
    public void shouldReturnNullForEmptyList() {
        var result = productImageMapper.getMainImage(List.of());
        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnNullForNullList() {
        var result = productImageMapper.getMainImage(null);
        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnSingleImage() {
        var image = ProductImage.builder()
                .id(1L)
                .url("/api/files/1.jpg")
                .sortOrder(0)
                .build();

        var result = productImageMapper.getMainImage(List.of(image));

        assertThat(result).isEqualTo("/api/files/1.jpg");
    }
}