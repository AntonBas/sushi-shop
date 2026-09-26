package com.sushishop.token;

import com.sushishop.mail.MailService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private TokenService tokenService;

    @Test
    public void shouldCreateVerificationTokenStoringOnlyItsHash() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .build();

        tokenService.createVerificationToken(user);

        var saved = ArgumentCaptor.forClass(Token.class);
        var sentToken = ArgumentCaptor.forClass(String.class);
        verify(tokenRepository).invalidateAllByUserAndType(1L, TokenType.EMAIL_VERIFICATION);
        verify(tokenRepository).save(saved.capture());
        verify(mailService).sendVerificationEmail(eq("anton@example.com"), sentToken.capture());
        assertThat(saved.getValue().getTokenType()).isEqualTo(TokenType.EMAIL_VERIFICATION);
        assertThat(saved.getValue().getUser()).isEqualTo(user);
        assertThat(saved.getValue().getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(saved.getValue().getToken())
                .isNotEqualTo(sentToken.getValue())
                .isEqualTo(TokenService.hash(sentToken.getValue()));
    }

    @Test
    public void shouldCreatePasswordResetToken() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .build();

        tokenService.createPasswordResetToken(user);

        var saved = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).invalidateAllByUserAndType(1L, TokenType.PASSWORD_RESET);
        verify(tokenRepository).save(saved.capture());
        verify(mailService).sendPasswordResetEmail(eq("anton@example.com"), any());
        assertThat(saved.getValue().getTokenType()).isEqualTo(TokenType.PASSWORD_RESET);
    }

    @Test
    public void shouldCreateEmailChangeToken() {
        var user = User.builder()
                .id(1L)
                .email("anton@example.com")
                .build();

        tokenService.createEmailChangeToken(user, "new@example.com");

        var saved = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).invalidateAllByUserAndType(1L, TokenType.EMAIL_CHANGE);
        verify(tokenRepository).save(saved.capture());
        verify(mailService).sendEmailChangeVerification(eq("new@example.com"), any());
        assertThat(saved.getValue().getTokenType()).isEqualTo(TokenType.EMAIL_CHANGE);
    }

    @Test
    public void shouldValidateToken() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(false)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(User.builder().build())
                .build();

        when(tokenRepository.findByToken(TokenService.hash("token123"))).thenReturn(Optional.of(token));

        var result = tokenService.validateAndGetToken("token123", TokenType.EMAIL_VERIFICATION);

        assertThat(result).isEqualTo(token);
    }

    @Test
    public void shouldThrowWhenTokenUsed() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(true)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(User.builder().build())
                .build();

        when(tokenRepository.findByToken(TokenService.hash("token123"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.validateAndGetToken("token123", TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token already used");
    }

    @Test
    public void shouldThrowWhenTokenExpired() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(false)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .user(User.builder().build())
                .build();

        when(tokenRepository.findByToken(TokenService.hash("token123"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.validateAndGetToken("token123", TokenType.EMAIL_VERIFICATION))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    public void shouldThrowWhenWrongTokenType() {
        var token = Token.builder()
                .token("token123")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .used(false)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .user(User.builder().build())
                .build();

        when(tokenRepository.findByToken(TokenService.hash("token123"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.validateAndGetToken("token123", TokenType.PASSWORD_RESET))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid token type");
    }
}