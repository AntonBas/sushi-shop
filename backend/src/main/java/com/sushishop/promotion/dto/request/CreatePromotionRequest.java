package com.sushishop.promotion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Discount percentage", example = "20.00")
        BigDecimal discountPercent,

        @NotNull(message = "Start date is required")
        @Schema(description = "Start date")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        @Schema(description = "End date")
        LocalDateTime endDate,

        @Schema(description = "Product IDs to include")
        List<Long> productIds
) {
}
