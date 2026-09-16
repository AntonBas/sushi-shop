package com.sushishop.scheduler;

import com.sushishop.token.TokenRepository;
import com.sushishop.user.UserRepository;
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
class UnverifiedAccountCleanupSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private UnverifiedAccountCleanupScheduler scheduler;

    @Test
    void shouldDeleteTokensBeforeDeletingUnverifiedUsersOlderThanGracePeriod() {
        when(userRepository.deleteAllByEmailVerifiedFalseAndCreatedAtBefore(any())).thenReturn(2);

        scheduler.cleanupUnverifiedAccounts();

        var cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenRepository).deleteAllByUnverifiedUserCreatedBefore(cutoffCaptor.capture());
        verify(userRepository).deleteAllByEmailVerifiedFalseAndCreatedAtBefore(cutoffCaptor.getValue());
        assertThat(Duration.between(cutoffCaptor.getValue(), LocalDateTime.now().minusHours(48)).abs())
                .isLessThan(Duration.ofSeconds(5));
    }

    @Test
    void shouldNotThrowWhenNothingToClean() {
        when(userRepository.deleteAllByEmailVerifiedFalseAndCreatedAtBefore(any())).thenReturn(0);

        scheduler.cleanupUnverifiedAccounts();

        verify(userRepository).deleteAllByEmailVerifiedFalseAndCreatedAtBefore(any());
    }
}
