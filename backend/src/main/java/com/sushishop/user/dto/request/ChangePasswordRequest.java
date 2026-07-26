package com.sushishop.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Current password", example = "oldPassword123")
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @Schema(description = "New password (min 8 chars, must contain letter and number)", example = "NewPass123")
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
        String newPassword
) {
}