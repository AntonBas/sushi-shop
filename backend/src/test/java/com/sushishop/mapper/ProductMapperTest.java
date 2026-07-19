package com.sushishop.mapper;

import com.sushishop.domain.Product;
import com.sushishop.domain.enums.Category;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

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
                .weight("250g")
                .pieces(8)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.description()).isEqualTo("Salmon roll");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo(Category.ROLL);
        assertThat(response.available()).isTrue();
        assertThat(response.weight()).isEqualTo("250g");
        assertThat(response.pieces()).isEqualTo(8);
        assertThat(response.images()).isNull();
        assertThat(response.reviewCount()).isNull();
        assertThat(response.averageRating()).isNull();
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
                .weight("250g")
                .pieces(8)
                .build();

        ProductListResponse response = productMapper.toListResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo(Category.ROLL);
        assertThat(response.available()).isTrue();
        assertThat(response.weight()).isEqualTo("250g");
        assertThat(response.pieces()).isEqualTo(8);
        assertThat(response.mainImage()).isNull();
        assertThat(response.averageRating()).isNull();
        assertThat(response.discountedPrice()).isNull();
    }
}