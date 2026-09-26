package com.sushishop.user.dto.request;

import com.sushishop.shared.service.EmailNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(
        @Schema(description = "New email address", example = "new-address@example.com")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String newEmail
) {

    public ChangeEmailRequest {
        newEmail = EmailNormalizer.normalize(newEmail);
    }
}
