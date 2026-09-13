package com.sushishop.promotion.dto.request;

import com.sushishop.promotion.PromotionConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request to create a promotion")
public record CreatePromotionRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 50, message = "Title must be less than 50 characters")
        @Schema(description = "Promotion title", example = "Weekend Sale")
        String title,

        @Size(max = 250, message = "Description must be less than 250 characters")
        @Schema(description = "Promotion description", example = "20% off on all rolls")
        String description,

        @NotNull(message = "Discount is required")
        @Positive(message = "Discount must be greater than 0")
        @DecimalMax(value = PromotionConstants.MAX_DISCOUNT_PERCENT, message = "Discount cannot exceed 90%")
        @Digits(integer = 3, fraction = 2, message = "Discount must have at most 2 decimal places")
        @Schema(description = "Discount percentage", example = "20.00")
        BigDecimal discountPercent,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        @Schema(description = "Start date")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        @Schema(description = "End date")
        LocalDateTime endDate,

        @Size(min = 1, max = 100, message = "Product list must contain between 1 and 100 items")
        @Schema(description = "Product IDs to include")
        List<Long> productIds
) {
}