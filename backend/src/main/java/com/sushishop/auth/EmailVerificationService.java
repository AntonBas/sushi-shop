package com.sushishop.auth;

import com.sushishop.token.TokenService;
import com.sushishop.token.TokenType;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

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
}