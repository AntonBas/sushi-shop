package com.sushishop.auth;

import com.sushishop.mail.MailService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public void forgotPassword(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("Password reset requested for unknown email {}", email);
            return;
        }

        var user = userOpt.get();
        if (!user.isEmailVerified()) {
            log.info("Password reset requested for unverified email {}", email);
            return;
        }

        tokenService.createPasswordResetToken(user);
        log.info("Password reset email sent to {}", email);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        var tokenEntity = tokenService.validateAndGetToken(resetToken, TokenType.PASSWORD_RESET);
        var user = tokenEntity.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        tokenService.invalidateAllByUserAndType(user.getId(), TokenType.PASSWORD_RESET);
        mailService.sendPasswordChangedNotification(user.getEmail());
        log.info("Password reset for {}", user.getEmail());
    }
}