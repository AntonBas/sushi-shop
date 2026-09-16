package com.sushishop.scheduler;

import com.sushishop.token.TokenRepository;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnverifiedAccountCleanupScheduler {

    private static final int GRACE_PERIOD_HOURS = 48;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupUnverifiedAccounts() {
        var cutoff = LocalDateTime.now().minusHours(GRACE_PERIOD_HOURS);
        tokenRepository.deleteAllByUnverifiedUserCreatedBefore(cutoff);
        int deleted = userRepository.deleteAllByEmailVerifiedFalseAndCreatedAtBefore(cutoff);
        log.info("Cleaned up {} unverified accounts older than {}h", deleted, GRACE_PERIOD_HOURS);
    }
}
