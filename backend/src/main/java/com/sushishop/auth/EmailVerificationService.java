package com.sushishop.auth;

import com.sushishop.audit.Auditable;
import com.sushishop.shared.enums.AuditAction;
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

    @Auditable(action = AuditAction.UPDATE, entity = "User", description = "Email verified")
    @Transactional
    public Long verifyEmail(String verificationToken) {
        if (tokenService.findTokenOwner(verificationToken, TokenType.EMAIL_VERIFICATION)
                .filter(User::isEmailVerified).isPresent()) {
            throw new BadRequestException("Email already verified", "EMAIL_ALREADY_VERIFIED");
        }
        var tokenEntity = tokenService.validateAndGetToken(verificationToken, TokenType.EMAIL_VERIFICATION);

        var user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenService.invalidateAllByUserAndType(user.getId(), TokenType.EMAIL_VERIFICATION);
        log.info("Email verified for {}", user.getEmail());
        return user.getId();
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
            log.info("Resend verification requested for already verified email {}", email);
            return (int) RESEND_COOLDOWN.toSeconds();
        }

        var lastSentAt = user.getLastVerificationEmailSentAt();
        if (lastSentAt != null) {
            long remaining = remainingCooldownSeconds(lastSentAt);
            if (remaining > 0) {
                throw new RateLimitExceededException(remaining);
            }
        }

        sendVerification(user);
        log.info("Verification email resent to {}", email);
        return (int) RESEND_COOLDOWN.toSeconds();
    }

    @Transactional
    public void resendIfCooldownElapsed(User user) {
        var lastSentAt = user.getLastVerificationEmailSentAt();
        if (lastSentAt != null && remainingCooldownSeconds(lastSentAt) > 0) {
            log.info("Verification email for {} within cooldown, skipping", user.getEmail());
            return;
        }
        sendVerification(user);
        log.info("Verification email sent to {} instead of password reset", user.getEmail());
    }

    private void sendVerification(User user) {
        tokenService.createVerificationToken(user);
        user.setLastVerificationEmailSentAt(LocalDateTime.now());
        userRepository.save(user);
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