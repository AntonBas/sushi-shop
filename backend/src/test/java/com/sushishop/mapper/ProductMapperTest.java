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
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .description("Salmon roll")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.description()).isEqualTo("Salmon roll");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo("ROLL");
        assertThat(response.available()).isTrue();
        assertThat(response.images()).isNull();
        assertThat(response.reviews()).isNull();
        assertThat(response.discountedPrice()).isNull();
    }

    @Test
    void shouldMapToListResponse() {
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .build();

        ProductListResponse response = productMapper.toListResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo("ROLL");
        assertThat(response.available()).isTrue();
        assertThat(response.mainImage()).isNull();
        assertThat(response.averageRating()).isNull();
        assertThat(response.discountedPrice()).isNull();
    }
}