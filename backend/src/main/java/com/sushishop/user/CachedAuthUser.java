package com.sushishop.user;

public record CachedAuthUser(String email, UserRole userRole, boolean emailVerified) {
}
