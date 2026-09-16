package com.sushishop.auth;

import com.sushishop.mail.MailService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.token.Token;
import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.User;
import com.sushishop.user.UserMapper;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserRole;
import com.sushishop.user.dto.request.ChangeEmailRequest;
import com.sushishop.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailChangeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private MailService mailService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache usersCache;

    @InjectMocks
    private EmailChangeService emailChangeService;

    @Test
    void shouldRequestEmailChange() {
        var request = new ChangeEmailRequest("new@example.com");
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        emailChangeService.requestEmailChange("anton@example.com", request);

        assertThat(user.getPendingEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(user);
        verify(tokenService).createEmailChangeToken(user, "new@example.com");
        verify(mailService).sendEmailChangeRequestedNotification("anton@example.com", "new@example.com");
    }

    @Test
    void shouldThrowWhenRequestingEmailChangeForUnverifiedUser() {
        var request = new ChangeEmailRequest("new@example.com");
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailChangeService.requestEmailChange("anton@example.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Please verify your current email first");
    }

    @Test
    void shouldThrowWhenNewEmailSameAsCurrent() {
        var request = new ChangeEmailRequest("anton@example.com");
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailChangeService.requestEmailChange("anton@example.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("New email must be different from current email");
    }

    @Test
    void shouldThrowWhenNewEmailAlreadyTaken() {
        var request = new ChangeEmailRequest("taken@example.com");
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> emailChangeService.requestEmailChange("anton@example.com", request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldConfirmEmailChange() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .pendingEmail("new@example.com")
                .tokenVersion(0)
                .build();
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_CHANGE)
                .user(user)
                .build();

        when(tokenService.validateAndGetToken("token123", TokenType.EMAIL_CHANGE)).thenReturn(token);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(cacheManager.getCache("users")).thenReturn(usersCache);
        when(userMapper.toResponse(user)).thenReturn(
                new UserResponse(1L, null, "new@example.com", null, null, UserRole.CUSTOMER, null, true));

        var result = emailChangeService.confirmEmailChange("token123");

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPendingEmail()).isNull();
        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).save(user);
        verify(tokenService).invalidateAllByUserAndType(1L, TokenType.EMAIL_CHANGE);
        verify(usersCache).evict("anton@example.com");
    }

    @Test
    void shouldThrowWhenConfirmingWithNoPendingEmail() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .pendingEmail(null)
                .build();
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_CHANGE)
                .user(user)
                .build();

        when(tokenService.validateAndGetToken("token123", TokenType.EMAIL_CHANGE)).thenReturn(token);

        assertThatThrownBy(() -> emailChangeService.confirmEmailChange("token123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No pending email change");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenConfirmedEmailTakenInTheMeantime() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .pendingEmail("new@example.com")
                .build();
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_CHANGE)
                .user(user)
                .build();

        when(tokenService.validateAndGetToken("token123", TokenType.EMAIL_CHANGE)).thenReturn(token);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> emailChangeService.confirmEmailChange("token123"))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }
}
