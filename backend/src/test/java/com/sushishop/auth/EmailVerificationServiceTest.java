package com.sushishop.auth;

import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.RateLimitExceededException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    public void shouldVerifyEmail() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(false)
                .build();

        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .user(user)
                .build();

        when(tokenService.validateAndGetToken("token123", TokenType.EMAIL_VERIFICATION))
                .thenReturn(token);

        emailVerificationService.verifyEmail("token123");

        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(tokenService).invalidateAllByUserAndType(1L, TokenType.EMAIL_VERIFICATION);
    }

    @Test
    public void shouldResendVerificationForUnverifiedUser() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmailForUpdate("anton@example.com")).thenReturn(Optional.of(user));

        int cooldownSeconds = emailVerificationService.resendVerification("anton@example.com");

        verify(tokenService).createVerificationToken(user);
        verify(userRepository).save(user);
        assertThat(user.getLastVerificationEmailSentAt()).isNotNull();
        assertThat(cooldownSeconds).isEqualTo(60);
    }

    @Test
    public void shouldRejectResendWhenAlreadyVerified() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findByEmailForUpdate("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailVerificationService.resendVerification("anton@example.com"))
                .isInstanceOf(BadRequestException.class);

        verify(tokenService, never()).createVerificationToken(user);
    }

    @Test
    public void shouldRejectResendWithinCooldownAndReportRemainingSeconds() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(false)
                .lastVerificationEmailSentAt(LocalDateTime.now().minusSeconds(30))
                .build();

        when(userRepository.findByEmailForUpdate("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailVerificationService.resendVerification("anton@example.com"))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> assertThat(((RateLimitExceededException) ex).getRetryAfterSeconds())
                        .isCloseTo(30L, org.assertj.core.data.Offset.offset(2L)));

        verify(tokenService, never()).createVerificationToken(user);
    }

    @Test
    public void shouldAllowResendAfterCooldownElapsed() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(false)
                .lastVerificationEmailSentAt(LocalDateTime.now().minusSeconds(61))
                .build();

        when(userRepository.findByEmailForUpdate("anton@example.com")).thenReturn(Optional.of(user));

        emailVerificationService.resendVerification("anton@example.com");

        verify(tokenService).createVerificationToken(user);
    }

    @Test
    public void shouldSilentlyIgnoreResendForUnknownEmailButReportSameCooldown() {
        when(userRepository.findByEmailForUpdate("unknown@example.com")).thenReturn(Optional.empty());

        int cooldownSeconds = emailVerificationService.resendVerification("unknown@example.com");

        verify(tokenService, never()).createVerificationToken(org.mockito.ArgumentMatchers.any());
        assertThat(cooldownSeconds).isEqualTo(60);
    }

    @Test
    public void shouldReportZeroCooldownForUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThat(emailVerificationService.getResendCooldownStatus("unknown@example.com")).isZero();
    }

    @Test
    public void shouldReportZeroCooldownForAlreadyVerifiedEmail() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(true)
                .lastVerificationEmailSentAt(LocalDateTime.now().minusSeconds(5))
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThat(emailVerificationService.getResendCooldownStatus("anton@example.com")).isZero();
    }

    @Test
    public void shouldReportRemainingCooldownForRecentlyResentEmail() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .emailVerified(false)
                .lastVerificationEmailSentAt(LocalDateTime.now().minusSeconds(40))
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThat(emailVerificationService.getResendCooldownStatus("anton@example.com"))
                .isCloseTo(20, org.assertj.core.data.Offset.offset(2));
    }
}