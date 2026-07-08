package com.sushishop.service;

import com.sushishop.domain.Token;
import com.sushishop.domain.enums.TokenType;
import com.sushishop.dto.request.LoginRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.AuthResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.repository.TokenRepository;
import com.sushishop.repository.UserRepository;
import com.sushishop.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

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
        String token = jwtUtil.generateToken(request.email(), role);

        var userResponse = userService.getByEmail(request.email());
        log.info("Login successful for email: {}", request.email());
        return new AuthResponse(token, userResponse);
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.email());

        var user = userService.create(request);
        String token = jwtUtil.generateToken(user.email(), user.role());

        log.info("Register successful for email: {}", request.email());
        return new AuthResponse(token, user);
    }

    public void verifyEmail(String token) {
        var tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (tokenEntity.isUsed()) {
            throw new BadRequestException("Token already used");
        }

        var user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);
        log.info("Email verified for {}", user.getEmail());
    }

    public void forgotPassword(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email first");
        }

        var token = Token.builder()
                .token(UUID.randomUUID().toString())
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .build();

        tokenRepository.save(token);
        mailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
        log.info("Password reset email sent to {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        var tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (tokenEntity.isUsed()) {
            throw new BadRequestException("Token already used");
        }

        var user = tokenEntity.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);
        log.info("Password reset for {}", user.getEmail());
    }
}