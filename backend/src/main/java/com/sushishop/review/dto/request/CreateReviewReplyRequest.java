package com.sushishop.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a review reply")
public record CreateReviewReplyRequest(
        @Schema(description = "Reply message", example = "Thank you for your feedback!")
        @NotBlank(message = "Message is required")
        @Size(max = 250, message = "Message must be less than 250 characters")
        String message
) {
}