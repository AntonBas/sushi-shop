package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Reset password request")
public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        @Schema(description = "Reset token")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 32, message = "Password must be between 8 and 64 characters")
        @Schema(description = "New password", example = "NewPassword123")
        String newPassword
) {
}
