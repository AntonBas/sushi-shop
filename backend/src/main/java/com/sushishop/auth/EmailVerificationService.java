package com.sushishop.auth;

import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.RateLimitExceededException;
import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Transactional
    public void verifyEmail(String verificationToken) {
        var tokenEntity = tokenService.validateAndGetToken(verificationToken, TokenType.EMAIL_VERIFICATION);

        var user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenService.invalidateAllByUserAndType(user.getId(), TokenType.EMAIL_VERIFICATION);
        log.info("Email verified for {}", user.getEmail());
    }

    @Transactional
    public int resendVerification(String email) {
        var userOpt = userRepository.findByEmailForUpdate(email);
        if (userOpt.isEmpty()) {
            log.info("Resend verification requested for unknown email {}", email);
            return (int) RESEND_COOLDOWN.toSeconds();
        }

        var user = userOpt.get();
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified");
        }

        var lastSentAt = user.getLastVerificationEmailSentAt();
        if (lastSentAt != null) {
            long remaining = remainingCooldownSeconds(lastSentAt);
            if (remaining > 0) {
                throw new RateLimitExceededException(remaining);
            }
        }

        tokenService.createVerificationToken(user);
        user.setLastVerificationEmailSentAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Verification email resent to {}", email);
        return (int) RESEND_COOLDOWN.toSeconds();
    }

    @Transactional(readOnly = true)
    public int getResendCooldownStatus(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> !user.isEmailVerified())
                .map(User::getLastVerificationEmailSentAt)
                .map(this::remainingCooldownSeconds)
                .filter(remaining -> remaining > 0)
                .map(Long::intValue)
                .orElse(0);
    }

    private long remainingCooldownSeconds(LocalDateTime lastSentAt) {
        var remaining = Duration.between(LocalDateTime.now(), lastSentAt.plus(RESEND_COOLDOWN));
        return remaining.isNegative() ? 0 : remaining.toSeconds();
    }
}