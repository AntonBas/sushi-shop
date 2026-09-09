package com.sushishop.auth;

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

import static org.assertj.core.api.Assertions.assertThat;
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
}