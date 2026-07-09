package com.sushishop.controller;

import com.sushishop.config.ratelimit.RateLimit;
import com.sushishop.dto.request.ForgotPasswordRequest;
import com.sushishop.dto.request.LoginRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.request.ResetPasswordRequest;
import com.sushishop.dto.response.AuthResponse;
import com.sushishop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    @RateLimit(value = 3)
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "User registered successfully"), @ApiResponse(responseCode = "400", description = "Invalid input data"), @ApiResponse(responseCode = "409", description = "Email already registered")})
    @SecurityRequirements()
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email: {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @RateLimit
    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Login successful"), @ApiResponse(responseCode = "401", description = "Invalid email or password")})
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

    @RateLimit(value = 10)
    @GetMapping("/verify")
    @Operation(summary = "Verify email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @RateLimit(duration = 900)
    @PostMapping("/password/forgot")
    @Operation(summary = "Request password reset")
    @SecurityRequirements()
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/auth/password/forgot - {}", request.email());
        authService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @RateLimit
    @PostMapping("/password/reset")
    @Operation(summary = "Reset password")
    @SecurityRequirements()
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/auth/password/reset");
        authService.resetPassword(request.token(), request.newPassword(), request.confirmPassword());
        return ResponseEntity.ok().build();
    }
}
