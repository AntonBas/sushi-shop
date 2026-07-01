package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a review")
public record CreateReviewRequest(
        @NotNull
        @Schema(description = "Product ID", example = "1")
        Long productId,

        @NotNull
        @Min(1)
        @Max(5)
        @Schema(description = "Rating from 1 to 5", example = "5")
        Integer rating,

        @Size(max = 200)
        @Schema(description = "Review comment", example = "Very tasty rolls!")
        String comment
) {
}
