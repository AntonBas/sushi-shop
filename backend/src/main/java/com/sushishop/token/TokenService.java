package com.sushishop.token;

import com.sushishop.mail.MailService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final int TOKEN_EXPIRATION_HOURS = 1;

    private final TokenRepository tokenRepository;
    private final MailService mailService;

    @Transactional
    public void createVerificationToken(User user) {
        String rawToken = issueToken(user, TokenType.EMAIL_VERIFICATION);
        mailService.sendVerificationEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public void createPasswordResetToken(User user) {
        String rawToken = issueToken(user, TokenType.PASSWORD_RESET);
        mailService.sendPasswordResetEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public void createEmailChangeToken(User user, String newEmail) {
        String rawToken = issueToken(user, TokenType.EMAIL_CHANGE);
        mailService.sendEmailChangeVerification(newEmail, rawToken);
    }

    @Transactional(readOnly = true)
    public Token validateAndGetToken(String tokenValue, TokenType expectedType) {
        var tokenEntity = tokenRepository.findByToken(hash(tokenValue))
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (tokenEntity.isUsed()) {
            throw new BadRequestException("Token already used");
        }

        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        if (tokenEntity.getTokenType() != expectedType) {
            throw new BadRequestException("Invalid token type");
        }

        return tokenEntity;
    }

    @Transactional
    public void invalidateAllByUserAndType(Long userId, TokenType type) {
        tokenRepository.invalidateAllByUserAndType(userId, type);
    }

    static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String issueToken(User user, TokenType type) {
        tokenRepository.invalidateAllByUserAndType(user.getId(), type);

        String rawToken = UUID.randomUUID().toString();
        tokenRepository.save(Token.builder()
                .token(hash(rawToken))
                .tokenType(type)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS))
                .build());
        return rawToken;
    }
}
