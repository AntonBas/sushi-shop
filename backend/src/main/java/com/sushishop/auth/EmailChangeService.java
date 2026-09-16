package com.sushishop.auth;

import com.sushishop.mail.MailService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.UserMapper;
import com.sushishop.user.UserRepository;
import com.sushishop.user.dto.request.ChangeEmailRequest;
import com.sushishop.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChangeService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final MailService mailService;
    private final CacheManager cacheManager;

    @Transactional
    public void requestEmailChange(String email, ChangeEmailRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your current email first");
        }

        if (request.newEmail().equals(email)) {
            throw new BadRequestException("New email must be different from current email");
        }

        if (userRepository.existsByEmail(request.newEmail())) {
            throw new ConflictException("Email already exists!");
        }

        user.setPendingEmail(request.newEmail());
        userRepository.save(user);

        tokenService.createEmailChangeToken(user, request.newEmail());
        mailService.sendEmailChangeRequestedNotification(email, request.newEmail());
        log.info("Email change requested for {} -> {}", email, request.newEmail());
    }

    @Transactional
    public UserResponse confirmEmailChange(String token) {
        var tokenEntity = tokenService.validateAndGetToken(token, TokenType.EMAIL_CHANGE);
        var user = tokenEntity.getUser();

        if (user.getPendingEmail() == null) {
            throw new BadRequestException("No pending email change for this account");
        }

        if (userRepository.existsByEmail(user.getPendingEmail())) {
            throw new ConflictException("Email already exists!");
        }

        var oldEmail = user.getEmail();
        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        tokenService.invalidateAllByUserAndType(user.getId(), TokenType.EMAIL_CHANGE);
        evictUserCache(oldEmail);
        log.info("Email changed from {} to {}", oldEmail, user.getEmail());
        return userMapper.toResponse(user);
    }

    private void evictUserCache(String email) {
        var usersCache = cacheManager.getCache("users");
        if (usersCache != null) {
            usersCache.evict(email);
        }
    }
}
