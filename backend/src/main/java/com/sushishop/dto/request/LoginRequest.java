package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequest(
        @Schema(description = "User email", example = "user@example.com")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "User password", example = "password123")
        @NotBlank(message = "Password is required")
        String password
) {
}
