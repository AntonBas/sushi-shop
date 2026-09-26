package com.sushishop.promotion;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionTest {

    @Test
    void shouldApplyFractionalDiscountPercentWithoutRoundingItFirst() {
        var promotion = Promotion.builder().discountPercent(new BigDecimal("12.50")).build();

        assertThat(promotion.applyDiscount(new BigDecimal("100.00"))).isEqualByComparingTo("87.50");
    }

    @Test
    void shouldRoundDiscountedPriceToCents() {
        var promotion = Promotion.builder().discountPercent(new BigDecimal("15")).build();

        assertThat(promotion.applyDiscount(new BigDecimal("33.33"))).isEqualByComparingTo("28.33");
    }

    @Test
    void shouldAllowEndedPromotionToPassEntityValidation() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var promotion = Promotion.builder().endDate(LocalDateTime.now().minusDays(1)).build();

        assertThat(validator.validateProperty(promotion, "endDate")).isEmpty();
    }
}
