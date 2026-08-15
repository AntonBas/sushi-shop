package com.sushishop.promotion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request to update a promotion")
public record UpdatePromotionRequest(
        @Size(max = 50, message = "Title must be less than 50 characters")
        @Schema(description = "Promotion title", example = "Weekend Sale")
        String title,

        @Size(max = 250, message = "Description must be less than 250 characters")
        @Schema(description = "Promotion description", example = "20% off on all rolls")
        String description,

        @Positive(message = "Discount must be greater than 0")
        @DecimalMax(value = "90.00", message = "Discount cannot exceed 90%")
        @Digits(integer = 3, fraction = 2, message = "Discount must have at most 2 decimal places")
        @Schema(description = "Discount percentage", example = "20.00")
        BigDecimal discountPercent,

        @Schema(description = "Start date")
        LocalDateTime startDate,

        @Schema(description = "End date")
        LocalDateTime endDate,

        @Schema(description = "Product IDs to include")
        List<Long> productIds,

        @Schema(description = "Is promotion active", example = "true")
        Boolean active
) {
}