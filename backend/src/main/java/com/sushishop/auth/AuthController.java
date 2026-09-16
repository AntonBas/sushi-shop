package com.sushishop.auth;

import com.sushishop.auth.dto.request.ForgotPasswordRequest;
import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.auth.dto.request.OAuth2ExchangeRequest;
import com.sushishop.auth.dto.request.ResendVerificationRequest;
import com.sushishop.auth.dto.request.ResetPasswordRequest;
import com.sushishop.auth.dto.response.AuthResponse;
import com.sushishop.auth.dto.response.ResendVerificationResponse;
import com.sushishop.security.jwt.JwtCookieService;
import com.sushishop.shared.ratelimit.RateLimit;
import com.sushishop.user.dto.request.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final JwtCookieService jwtCookieService;

    @RateLimit(value = 3)
    @PostMapping("/register")
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @SecurityRequirements()
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        log.info("POST /api/auth/register - email: {}", request.email());
        var result = authService.register(request);
        jwtCookieService.addTokenCookie(response, result.token());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(result.user()));
    }

    @RateLimit(key = {"ip", "email"})
    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @SecurityRequirements()
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        log.info("POST /api/auth/login - email: {}", request.email());
        var result = authService.login(request);
        jwtCookieService.addTokenCookie(response, result.token());
        return ResponseEntity.ok(new AuthResponse(result.user()));
    }

    @RateLimit(value = 10)
    @PostMapping("/oauth2/exchange")
    @Operation(summary = "Exchange OAuth2 one-time code for a JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code")
    })
    @SecurityRequirements()
    public ResponseEntity<AuthResponse> exchangeOAuth2Code(@Valid @RequestBody OAuth2ExchangeRequest request, HttpServletResponse response) {
        var result = authService.exchangeOAuth2Code(request.code());
        jwtCookieService.addTokenCookie(response, result.token());
        return ResponseEntity.ok(new AuthResponse(result.user()));
    }

    @RateLimit(value = 10)
    @PostMapping("/logout")
    @Operation(summary = "Log out the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @SecurityRequirements()
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(jwtCookieService.extractToken(request));
        jwtCookieService.clearTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @RateLimit(value = 20)
    @GetMapping("/ws-ticket")
    @Operation(summary = "Issue a one-time ticket for WebSocket authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket issued")
    })
    public ResponseEntity<String> issueWsTicket(Authentication authentication) {
        return ResponseEntity.ok(authService.issueWsTicket(authentication.getName()));
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
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent, or silently ignored if email is unknown"),
            @ApiResponse(responseCode = "400", description = "Email already verified"),
            @ApiResponse(responseCode = "429", description = "Resend requested too soon")
    })
    @SecurityRequirements()
    public ResponseEntity<ResendVerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("POST /api/auth/resend-verification - {}", request.email());
        int cooldownSeconds = emailVerificationService.resendVerification(request.email());
        return ResponseEntity.ok(new ResendVerificationResponse(cooldownSeconds));
    }

    @RateLimit(value = 10)
    @GetMapping("/resend-verification/status")
    @Operation(summary = "Check remaining resend cooldown without sending an email")
    @SecurityRequirements()
    public ResponseEntity<ResendVerificationResponse> resendVerificationStatus(@RequestParam String email) {
        int cooldownSeconds = emailVerificationService.getResendCooldownStatus(email);
        return ResponseEntity.ok(new ResendVerificationResponse(cooldownSeconds));
    }

    @RateLimit(duration = 900)
    @PostMapping("/password/forgot")
    @Operation(summary = "Request password reset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Always returned, regardless of whether the email is registered or verified")
    })
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
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}