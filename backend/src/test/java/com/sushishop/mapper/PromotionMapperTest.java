package com.sushishop.mapper;

import com.sushishop.domain.Product;
import com.sushishop.domain.Promotion;
import com.sushishop.domain.enums.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class PromotionMapperTest {

    @Autowired
    private PromotionMapper promotionMapper;

    @Test
    void shouldMapToResponse() {
        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .build();

        var promotion = Promotion.builder()
                .id(1L)
                .title("Weekend Sale")
                .description("20% off")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .active(true)
                .products(Set.of(product))
                .build();

        var response = promotionMapper.toResponse(promotion);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Weekend Sale");
        assertThat(response.products()).hasSize(1);
    }
}