package com.sushishop.service;

import com.sushishop.dto.request.LoginRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLogin() {
        var request = new LoginRequest("anton@example.com", "password123");
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);
        var authority = new SimpleGrantedAuthority("ROLE_CUSTOMER");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(inv -> List.of(authority));
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER")).thenReturn("jwt-token");
        when(userService.getByEmail("anton@example.com")).thenReturn(userResponse);

        var result = authService.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.user().email()).isEqualTo("anton@example.com");
    }

    @Test
    void shouldRegister() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);

        when(userService.create(request)).thenReturn(userResponse);
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER")).thenReturn("jwt-token");

        var result = authService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().role()).isEqualTo("CUSTOMER");
    }
}