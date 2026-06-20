package com.sushishop.dto.response;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role
) {
}
