package com.sushishop.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.auth.dto.request.ForgotPasswordRequest;
import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.auth.dto.request.OAuth2ExchangeRequest;
import com.sushishop.auth.dto.request.ResendVerificationRequest;
import com.sushishop.auth.dto.request.ResetPasswordRequest;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.RateLimitExceededException;
import com.sushishop.user.UserRole;
import com.sushishop.user.dto.request.RegisterRequest;
import com.sushishop.user.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldRegister() throws Exception {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", UserRole.CUSTOMER, null);
        var authResult = new AuthService.AuthResult("jwt-token", userResponse);

        when(authService.register(any())).thenReturn(authResult);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(cookie().value("jwt", "jwt-token"))
                .andExpect(jsonPath("$.user.email").value("anton@example.com"));
    }

    @Test
    public void shouldReturn400WhenInvalidRegister() throws Exception {
        var request = new RegisterRequest("", "invalid", "123", "123", "", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldLogin() throws Exception {
        var request = new LoginRequest("anton@example.com", "password123");
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", UserRole.CUSTOMER, null);
        var authResult = new AuthService.AuthResult("jwt-token", userResponse);

        when(authService.login(any())).thenReturn(authResult);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt", "jwt-token"))
                .andExpect(jsonPath("$.user.email").value("anton@example.com"));
    }

    @Test
    public void shouldReturn401WhenInvalidCredentials() throws Exception {
        var request = new LoginRequest("anton@example.com", "wrong");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldExchangeOAuth2Code() throws Exception {
        var request = new OAuth2ExchangeRequest("valid-code");
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", UserRole.CUSTOMER, null);
        var authResult = new AuthService.AuthResult("jwt-token", userResponse);

        when(authService.exchangeOAuth2Code("valid-code")).thenReturn(authResult);

        mockMvc.perform(post("/api/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt", "jwt-token"));
    }

    @Test
    public void shouldReturn400WhenOAuth2CodeInvalid() throws Exception {
        var request = new OAuth2ExchangeRequest("bad-code");

        when(authService.exchangeOAuth2Code("bad-code")).thenThrow(new BadRequestException("Invalid or expired code"));

        mockMvc.perform(post("/api/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldVerifyEmail() throws Exception {
        mockMvc.perform(get("/api/auth/verify")
                        .param("token", "token123"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldResendVerification() throws Exception {
        var request = new ResendVerificationRequest("anton@example.com");

        when(emailVerificationService.resendVerification("anton@example.com")).thenReturn(60);

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cooldownSeconds").value(60));
    }

    @Test
    public void shouldReturn400WhenResendingForAlreadyVerifiedEmail() throws Exception {
        var request = new ResendVerificationRequest("anton@example.com");

        when(emailVerificationService.resendVerification("anton@example.com"))
                .thenThrow(new BadRequestException("Email already verified"));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn429WithRetryAfterHeaderWhenResendCooldownActive() throws Exception {
        var request = new ResendVerificationRequest("anton@example.com");

        when(emailVerificationService.resendVerification("anton@example.com"))
                .thenThrow(new RateLimitExceededException(42));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().longValue("Retry-After", 42));
    }

    @Test
    public void shouldGetResendVerificationStatus() throws Exception {
        when(emailVerificationService.getResendCooldownStatus(anyString())).thenReturn(17);

        mockMvc.perform(get("/api/auth/resend-verification/status")
                        .param("email", "anton@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cooldownSeconds").value(17));
    }

    @Test
    public void shouldForgotPassword() throws Exception {
        var request = new ForgotPasswordRequest("anton@example.com");

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldResetPassword() throws Exception {
        var request = new ResetPasswordRequest("token123", "NewPass123", "NewPass123");

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("jwt", 0));
    }
}