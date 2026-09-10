package com.sushishop.user;

public record CachedAuthUser(String email, String password, UserRole userRole, boolean emailVerified) {
}
