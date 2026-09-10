package com.sushishop.user;

import jakarta.annotation.Nonnull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final UserRole userRole;
    private final boolean emailVerified;

    public CustomUserDetails(User user) {
        this(user.getEmail(), user.getPassword(), user.getUserRole(), user.isEmailVerified());
    }

    public CustomUserDetails(CachedAuthUser cachedUser) {
        this(cachedUser.email(), cachedUser.password(), cachedUser.userRole(), cachedUser.emailVerified());
    }

    private CustomUserDetails(String email, String password, UserRole userRole, boolean emailVerified) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.emailVerified = emailVerified;
    }

    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    @Nonnull
    public String getPassword() {
        return password;
    }

    @Override
    @Nonnull
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}
