package com.sushishop.auth;

import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.auth.dto.response.AuthResponse;
import com.sushishop.security.jwt.JwtUtil;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.token.TokenService;
import com.sushishop.user.UserMapper;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserService;
import com.sushishop.user.dto.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
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

        var userResponse = userMapper.toResponse(user);
        log.info("Login successful for email: {}", request.email());
        return new AuthResponse(jwt, userResponse);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.email());

        var user = userService.create(request);
        tokenService.createVerificationToken(user);

        String jwt = jwtUtil.generateToken(user.getEmail(), user.getUserRole().name(), user.getTokenVersion());
        var userResponse = userMapper.toResponse(user);

        log.info("Register successful for email: {}", request.email());
        return new AuthResponse(jwt, userResponse);
    }
}