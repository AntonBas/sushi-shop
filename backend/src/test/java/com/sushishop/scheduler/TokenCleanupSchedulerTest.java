package com.sushishop.scheduler;

import com.sushishop.token.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenCleanupScheduler scheduler;

    @Test
    void shouldDeleteExpiredUnusedTokensAsOfNow() {
        when(tokenRepository.deleteAllByExpiryDateBeforeAndUsedFalse(any())).thenReturn(3);

        scheduler.cleanupExpiredTokens();

        var cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenRepository).deleteAllByExpiryDateBeforeAndUsedFalse(cutoffCaptor.capture());
        assertThat(Duration.between(cutoffCaptor.getValue(), LocalDateTime.now()).abs())
                .isLessThan(Duration.ofSeconds(5));
    }

    @Test
    void shouldNotThrowWhenNothingToClean() {
        when(tokenRepository.deleteAllByExpiryDateBeforeAndUsedFalse(any())).thenReturn(0);

        scheduler.cleanupExpiredTokens();

        verify(tokenRepository).deleteAllByExpiryDateBeforeAndUsedFalse(any());
    }
}
