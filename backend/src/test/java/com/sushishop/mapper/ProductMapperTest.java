package com.sushishop.mapper;

import com.sushishop.domain.Product;
import com.sushishop.domain.ProductImage;
import com.sushishop.domain.enums.Category;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void shouldMapToResponse() {
        var images = List.of(
                ProductImage.builder().url("/api/files/abc.jpg").sortOrder(0).build(),
                ProductImage.builder().url("/api/files/def.jpg").sortOrder(1).build()
        );

        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .description("Salmon roll")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .productImages(images)
                .available(true)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.description()).isEqualTo("Salmon roll");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo("ROLL");
        assertThat(response.images()).hasSize(2);
        assertThat(response.images().getFirst()).isEqualTo("/api/files/abc.jpg");
        assertThat(response.available()).isTrue();
    }

    @Test
    void shouldMapToListResponse() {
        var images = List.of(
                ProductImage.builder().url("/api/files/main.jpg").sortOrder(0).build()
        );

        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .productImages(images)
                .available(true)
                .build();

        ProductListResponse response = productMapper.toListResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo("ROLL");
        assertThat(response.mainImage()).isEqualTo("/api/files/main.jpg");
        assertThat(response.available()).isTrue();
    }
}