package com.sushishop.service;

import com.sushishop.domain.Token;
import com.sushishop.domain.User;
import com.sushishop.domain.enums.TokenType;
import com.sushishop.dto.request.LoginRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.repository.TokenRepository;
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

import java.time.LocalDateTime;
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
    private TokenRepository tokenRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLogin() {
        var request = new LoginRequest("anton@example.com", "password123");
        var user = User.builder().email("anton@example.com").emailVerified(true).tokenVersion(0).build();
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);
        var authority = new SimpleGrantedAuthority("ROLE_CUSTOMER");

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(inv -> List.of(authority));
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0)).thenReturn("jwt-token");
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
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void shouldRegister() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);
        var user = User.builder().email("anton@example.com").tokenVersion(0).build();

        when(userService.create(request)).thenReturn(userResponse);
        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0)).thenReturn("jwt-token");

        var result = authService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void shouldVerifyEmail() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(false)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(User.builder().email("anton@example.com").emailVerified(false).build())
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        authService.verifyEmail("token123");

        assertThat(token.getUser().isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(tokenRepository).save(token);
    }

    @Test
    void shouldThrowWhenVerifyEmailTokenExpired() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(false)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .user(User.builder().email("anton@example.com").emailVerified(false).build())
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail("token123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void shouldThrowWhenVerifyEmailTokenAlreadyUsed() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(true)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(User.builder().email("anton@example.com").emailVerified(false).build())
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyEmail("token123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token already used");
    }

    @Test
    void shouldForgotPassword() {
        var user = User.builder().id(1L).email("anton@example.com").emailVerified(true).build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword("anton@example.com");

        verify(tokenRepository).invalidateAllByUserAndType(1L, TokenType.PASSWORD_RESET);
        verify(tokenRepository).save(any());
        verify(mailService).sendPasswordResetEmail(eq("anton@example.com"), any());
    }

    @Test
    void shouldNotRevealWhenForgotPasswordForUnverifiedUser() {
        var user = User.builder().email("anton@example.com").emailVerified(false).build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword("anton@example.com");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void shouldNotRevealWhenForgotPasswordForNonExistentUser() {
        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword("anton@example.com");

        verify(tokenRepository, never()).save(any());
        verify(mailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void shouldResetPassword() {
        var user = User.builder().id(1L).email("anton@example.com").password("oldHashed").tokenVersion(0).build();
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.PASSWORD_RESET)
                .used(false)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(user)
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("newPass123", "oldHashed")).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("hashed");

        authService.resetPassword("token123", "newPass123", "newPass123");

        assertThat(user.getPassword()).isEqualTo("hashed");
        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).save(user);
        verify(tokenRepository).invalidateAllByUserAndType(1L, TokenType.PASSWORD_RESET);
    }

    @Test
    void shouldThrowWhenResetPasswordMismatch() {
        assertThatThrownBy(() -> authService.resetPassword("token123", "newPass123", "differentPass"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Passwords don't match!");

        verify(tokenRepository, never()).findByToken(any());
    }

    @Test
    void shouldThrowWhenResetPasswordSameAsOld() {
        var user = User.builder().id(1L).email("anton@example.com").password("oldHashed").tokenVersion(0).build();
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.PASSWORD_RESET)
                .used(false)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(user)
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches("newPass123", "oldHashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.resetPassword("token123", "newPass123", "newPass123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("New password must be different from old password");
    }

    @Test
    void shouldThrowWhenResetPasswordTokenExpired() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.PASSWORD_RESET)
                .used(false)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .user(User.builder().build())
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword("token123", "newPass123", "newPass123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void shouldThrowWhenResetPasswordWrongTokenType() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(false)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(User.builder().build())
                .build();

        when(tokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword("token123", "newPass123", "newPass123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid token type");
    }
}