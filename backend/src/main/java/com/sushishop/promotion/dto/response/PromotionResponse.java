package com.sushishop.promotion.dto.response;

import com.sushishop.product.dto.response.ProductListResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Promotion response")
public record PromotionResponse(
        @Schema(description = "Promotion ID", example = "1")
        Long id,

        @Schema(description = "Promotion title", example = "Weekend Sale")
        String title,

        @Schema(description = "Promotion description", example = "20% off on all rolls")
        String description,

        @Schema(description = "Discount percentage", example = "20.00")
        BigDecimal discountPercent,

        @Schema(description = "Start date")
        LocalDateTime startDate,

        @Schema(description = "End date")
        LocalDateTime endDate,

        @Schema(description = "Is promotion active")
        boolean active,

        @Schema(description = "Products in promotion")
        List<ProductListResponse> products
) {
}
