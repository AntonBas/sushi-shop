package com.sushishop.service;

import com.sushishop.domain.User;
import com.sushishop.dto.request.LoginRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLogin() {
        var request = new LoginRequest("anton@example.com", "password123");
        var user = User.builder().email("anton@example.com").emailVerified(true).build();
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);
        var authority = new SimpleGrantedAuthority("ROLE_CUSTOMER");

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(inv -> List.of(authority));
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER")).thenReturn("jwt-token");
        when(userService.getByEmail("anton@example.com")).thenReturn(userResponse);

        var result = authService.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void shouldThrowWhenEmailNotVerified() {
        var request = new LoginRequest("anton@example.com", "password123");
        var user = User.builder().email("anton@example.com").emailVerified(false).build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("verify your email");
    }

    @Test
    void shouldRegister() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);

        when(userService.create(request)).thenReturn(userResponse);
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER")).thenReturn("jwt-token");

        var result = authService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void shouldVerifyEmail() {
        var user = User.builder().email("anton@example.com").emailVerified(false).verificationToken("token123").build();

        when(userRepository.findByVerificationToken("token123")).thenReturn(Optional.of(user));

        authService.verifyEmail("token123");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getVerificationToken()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void shouldForgotPassword() {
        var user = User.builder().email("anton@example.com").build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword("anton@example.com");

        assertThat(user.getVerificationToken()).isNotNull();
        verify(userRepository).save(user);
        verify(mailService).sendPasswordResetEmail(eq("anton@example.com"), any());
    }

    @Test
    void shouldResetPassword() {
        var user = User.builder().email("anton@example.com").verificationToken("token123").build();

        when(userRepository.findByVerificationToken("token123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass123")).thenReturn("hashed");

        authService.resetPassword("token123", "newPass123");

        assertThat(user.getPassword()).isEqualTo("hashed");
        assertThat(user.getVerificationToken()).isNull();
        verify(userRepository).save(user);
    }
}