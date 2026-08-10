package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.shared.enums.Category;
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
                .slug("weekend-sale")
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
        assertThat(response.slug()).isEqualTo("weekend-sale");
        assertThat(response.title()).isEqualTo("Weekend Sale");
        assertThat(response.products()).isNull();
    }
}