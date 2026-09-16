package com.sushishop.auth;

import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.security.jwt.JwtBlacklistService;
import com.sushishop.security.jwt.JwtUtil;
import com.sushishop.security.jwt.WsTicketService;
import com.sushishop.security.oauth2.OAuth2ExchangeCodeService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.token.TokenService;
import com.sushishop.user.UserMapper;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserService;
import com.sushishop.user.dto.request.RegisterRequest;
import com.sushishop.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final OAuth2ExchangeCodeService oAuth2ExchangeCodeService;
    private final WsTicketService wsTicketService;
    private final JwtBlacklistService jwtBlacklistService;

    public record AuthResult(String token, UserResponse user) {
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before login");
        }

        if (user.getPassword() == null) {
            throw new BadRequestException("This account uses Google sign-in and has no password set. Sign in with Google, or use \"Forgot password?\" to set one.");
        }

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var authority = authentication.getAuthorities().iterator().next().getAuthority();
        String role = authority != null ? authority.replace("ROLE_", "") : "CUSTOMER";
        String jwt = jwtUtil.generateToken(request.email(), role, user.getTokenVersion());

        var userResponse = userMapper.toResponse(user);
        log.info("Login successful for email: {}", request.email());
        return new AuthResult(jwt, userResponse);
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.email());

        var user = userService.create(request);
        tokenService.createVerificationToken(user);

        String jwt = jwtUtil.generateToken(user.getEmail(), user.getUserRole().name(), user.getTokenVersion());
        var userResponse = userMapper.toResponse(user);

        log.info("Register successful for email: {}", request.email());
        return new AuthResult(jwt, userResponse);
    }

    @Transactional(readOnly = true)
    public AuthResult exchangeOAuth2Code(String code) {
        String email = oAuth2ExchangeCodeService.consume(code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code"));

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code"));

        String jwt = jwtUtil.generateToken(user.getEmail(), user.getUserRole().name(), user.getTokenVersion());
        var userResponse = userMapper.toResponse(user);

        log.info("OAuth2 code exchange successful for email: {}", email);
        return new AuthResult(jwt, userResponse);
    }

    @Transactional(readOnly = true)
    public String issueWsTicket(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Unknown user"));
        return wsTicketService.issueTicket(email, user.getTokenVersion());
    }

    public void logout(String token) {
        if (token == null) {
            return;
        }

        var payload = jwtUtil.parseToken(token);
        if (payload == null || payload.jti() == null || payload.expiration() == null) {
            return;
        }

        var ttl = Duration.between(Instant.now(), payload.expiration().toInstant());
        jwtBlacklistService.blacklist(payload.jti(), ttl);
        log.info("Logout: token blacklisted for {}", payload.email());
    }
}