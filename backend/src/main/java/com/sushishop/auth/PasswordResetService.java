package com.sushishop.auth;

import com.sushishop.shared.enums.TokenType;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.token.TokenService;
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

    @Transactional
    public void forgotPassword(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email first");
        }

        tokenService.createPasswordResetToken(user);
        log.info("Password reset email sent to {}", email);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords don't match!");
        }

        var tokenEntity = tokenService.validateAndGetToken(resetToken, TokenType.PASSWORD_RESET);
        var user = tokenEntity.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        tokenService.invalidateAllByUserAndType(user.getId(), TokenType.PASSWORD_RESET);
        log.info("Password reset for {}", user.getEmail());
    }
}