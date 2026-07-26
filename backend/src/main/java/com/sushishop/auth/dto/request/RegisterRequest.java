package com.sushishop.auth.dto.request;

import com.sushishop.shared.address.AddressRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "Registration data")
public record RegisterRequest(
        @Schema(description = "Full name", example = "Anton Bas")
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be less than 50 characters")
        String name,

        @Schema(description = "Email address", example = "user@example.com")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @Schema(description = "Password (min 6 characters)", example = "password123")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "Confirm password", example = "password123")
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @Schema(description = "Phone number", example = "+380961791111")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
        @NotBlank(message = "Phone number is required")
        String phone,

        @Schema(description = "Default delivery address")
        @Valid
        AddressRequest address
) {
}