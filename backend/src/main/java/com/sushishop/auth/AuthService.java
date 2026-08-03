package com.sushishop.auth;

import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.auth.dto.request.RegisterRequest;
import com.sushishop.auth.dto.response.AuthResponse;
import com.sushishop.mail.MailService;
import com.sushishop.shared.enums.TokenType;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.security.jwt.JwtUtil;
import com.sushishop.user.Token;
import com.sushishop.user.TokenRepository;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int TOKEN_EXPIRATION_HOURS = 1;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before login");
        }

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var authority = authentication.getAuthorities().iterator().next().getAuthority();
        String role = authority != null ? authority.replace("ROLE_", "") : "CUSTOMER";
        String jwt = jwtUtil.generateToken(request.email(), role, user.getTokenVersion());

        var userResponse = userService.getByEmail(request.email());
        log.info("Login successful for email: {}", request.email());
        return new AuthResponse(jwt, userResponse);
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.email());

        var userResponse = userService.create(request);
        var user = userRepository.findByEmail(userResponse.email())
                .orElseThrow(() -> new BadRequestException("User not found after registration"));
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getUserRole().name(), user.getTokenVersion());

        log.info("Register successful for email: {}", request.email());
        return new AuthResponse(jwt, userResponse);
    }

    @Transactional
    public void verifyEmail(String verificationToken) {
        var tokenEntity = validateAndGetToken(verificationToken, TokenType.EMAIL_VERIFICATION);

        var user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);
        log.info("Email verified for {}", user.getEmail());
    }

    @Transactional
    public void forgotPassword(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email first");
        }

        tokenRepository.invalidateAllByUserAndType(user.getId(), TokenType.PASSWORD_RESET);

        var resetToken = Token.builder()
                .token(UUID.randomUUID().toString())
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS))
                .build();

        tokenRepository.save(resetToken);
        mailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
        log.info("Password reset email sent to {}", email);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords don't match!");
        }

        var tokenEntity = validateAndGetToken(resetToken, TokenType.PASSWORD_RESET);
        var user = tokenEntity.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        tokenRepository.invalidateAllByUserAndType(user.getId(), TokenType.PASSWORD_RESET);
        log.info("Password reset for {}", user.getEmail());
    }

    private Token validateAndGetToken(String tokenValue, TokenType expectedType) {
        var tokenEntity = tokenRepository.findByToken(tokenValue)
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
}