package com.sushishop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required") @Email
        String email,
        @NotBlank(message = "Password is required") @Size(min = 6)
        String password,
        @NotBlank(message = "Phone number is required")
        String phone
) {
}
