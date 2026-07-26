package com.sushishop.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Review response")
public record ReviewResponse(
        @Schema(description = "Review ID", example = "1")
        Long id,

        @Schema(description = "User name", example = "Anton")
        String userName,

        @Schema(description = "Rating", example = "5")
        Integer rating,

        @Schema(description = "Comment", example = "Very tasty!")
        String comment,

        @Schema(description = "Created date")
        LocalDateTime createdAt
) {
}
