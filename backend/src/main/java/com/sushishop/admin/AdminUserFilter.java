package com.sushishop.admin;

import com.sushishop.user.UserRole;

public record AdminUserFilter(String search, UserRole role, Boolean emailVerified) {
}
