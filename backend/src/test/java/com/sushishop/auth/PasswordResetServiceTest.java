package com.sushishop.auth;

import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.token.Token;
import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    public void shouldForgotPassword() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        passwordResetService.forgotPassword("anton@example.com");

        verify(tokenService).createPasswordResetToken(user);
    }

    @Test
    public void shouldThrowWhenForgotPasswordForUnverifiedUser() {
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> passwordResetService.forgotPassword("anton@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Please verify your email first");
    }

    @Test
    public void shouldThrowWhenForgotPasswordForNonExistentUser() {
        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.forgotPassword("anton@example.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldResetPassword() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .password("oldHashed")
                .tokenVersion(0)
                .build();

        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .build();

        when(tokenService.validateAndGetToken("token123", TokenType.PASSWORD_RESET))
                .thenReturn(token);
        when(passwordEncoder.matches("newPass123", "oldHashed")).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("hashed");

        passwordResetService.resetPassword("token123", "newPass123");

        assertThat(user.getPassword()).isEqualTo("hashed");
        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).save(user);
        verify(tokenService).invalidateAllByUserAndType(1L, TokenType.PASSWORD_RESET);
    }

    @Test
    public void shouldThrowWhenResetPasswordSameAsOld() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .password("oldHashed")
                .tokenVersion(0)
                .build();

        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .build();

        when(tokenService.validateAndGetToken("token123", TokenType.PASSWORD_RESET))
                .thenReturn(token);
        when(passwordEncoder.matches("newPass123", "oldHashed")).thenReturn(true);

        assertThatThrownBy(() -> passwordResetService.resetPassword("token123", "newPass123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("New password must be different from old password");
    }
}