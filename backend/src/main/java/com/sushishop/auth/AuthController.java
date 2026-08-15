package com.sushishop.auth;

import com.sushishop.auth.dto.request.ForgotPasswordRequest;
import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.auth.dto.request.RegisterRequest;
import com.sushishop.auth.dto.request.ResetPasswordRequest;
import com.sushishop.auth.dto.response.AuthResponse;
import com.sushishop.shared.ratelimit.RateLimit;
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
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @RateLimit(value = 3)
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @SecurityRequirements()
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email: {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @RateLimit
    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @SecurityRequirements()
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

    @RateLimit(value = 10)
    @GetMapping("/verify")
    @Operation(summary = "Verify email")
    @SecurityRequirements()
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @RateLimit(duration = 900)
    @PostMapping("/password/forgot")
    @Operation(summary = "Request password reset")
    @SecurityRequirements()
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/auth/password/forgot - {}", request.email());
        passwordResetService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @RateLimit
    @PostMapping("/password/reset")
    @Operation(summary = "Reset password")
    @SecurityRequirements()
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/auth/password/reset");
        passwordResetService.resetPassword(request.token(), request.newPassword(), request.confirmPassword());
        return ResponseEntity.ok().build();
    }
}