package com.sushishop.auth;

import com.sushishop.mail.MailService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Duration RESET_COOLDOWN = Duration.ofSeconds(60);

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void forgotPassword(String email) {
        var userOpt = userRepository.findByEmailForUpdate(email);
        if (userOpt.isEmpty()) {
            log.info("Password reset requested for unknown email {}", email);
            return;
        }

        var user = userOpt.get();
        if (!user.isEmailVerified()) {
            log.info("Password reset requested for unverified email {}", email);
            return;
        }

        var lastSentAt = user.getLastPasswordResetSentAt();
        if (lastSentAt != null && lastSentAt.plus(RESET_COOLDOWN).isAfter(LocalDateTime.now())) {
            log.info("Password reset requested for {} within cooldown, skipping", email);
            return;
        }

        tokenService.createPasswordResetToken(user);
        user.setLastPasswordResetSentAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Password reset email sent to {}", email);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        var tokenEntity = tokenService.validateAndGetToken(resetToken, TokenType.PASSWORD_RESET);
        var user = tokenEntity.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        var oldTokenVersion = user.getTokenVersion();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(oldTokenVersion + 1);
        userRepository.save(user);

        tokenService.invalidateAllByUserAndType(user.getId(), TokenType.PASSWORD_RESET);
        eventPublisher.publishEvent(new UserSessionsInvalidatedEvent(this, user.getEmail(), oldTokenVersion));
        mailService.sendPasswordChangedNotification(user.getEmail());
        log.info("Password reset for {}", user.getEmail());
    }
}