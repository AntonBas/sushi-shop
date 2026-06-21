package com.sushishop.service;

import com.sushishop.dto.request.LoginRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.AuthResponse;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        String token = jwtUtil.generateToken(request.email(),
                authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));

        UserResponse user = userService.getByEmail(request.email());
        log.info("Login successful for email: {}", request.email());
        return new AuthResponse(token, user);
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.email());

        UserResponse user = userService.create(request);
        String token = jwtUtil.generateToken(user.email(), user.role());

        log.info("Register successful for email: {}", request.email());
        return new AuthResponse(token, user);
    }
}
