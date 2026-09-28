package com.sushishop.user;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final UserRole userRole;

    public CustomUserDetails(User user) {
        this(user.getEmail(), user.getPassword(), user.getUserRole());
    }

    public CustomUserDetails(CachedAuthUser cachedUser) {
        this(cachedUser.email(), null, cachedUser.userRole());
    }

    private CustomUserDetails(String email, String password, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    @Nullable
    public String getPassword() {
        return password;
    }

    @Override
    @Nonnull
    public String getUsername() {
        return email;
    }
}
