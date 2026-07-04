package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a review")
public record CreateReviewRequest(
        @NotNull(message = "Product ID is required")
        @Schema(description = "Product ID", example = "1")
        Long productId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        @Schema(description = "Rating from 1 to 5", example = "5")
        Integer rating,

        @Size(max = 100, message = "Comment must be less than 100 characters")
        @Schema(description = "Review comment", example = "Very tasty rolls!")
        String comment
) {
}
