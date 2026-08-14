package com.sushishop.auth;

import com.sushishop.auth.dto.request.LoginRequest;
import com.sushishop.auth.dto.request.RegisterRequest;
import com.sushishop.shared.enums.UserRole;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.security.jwt.JwtUtil;
import com.sushishop.token.TokenService;
import com.sushishop.user.User;
import com.sushishop.user.UserMapper;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserService;
import com.sushishop.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    public void shouldLogin() {
        var request = new LoginRequest("anton@example.com", "password123");
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(true)
                .tokenVersion(0)
                .build();
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", UserRole.CUSTOMER, null);
        var authority = new SimpleGrantedAuthority("ROLE_CUSTOMER");

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(inv -> List.of(authority));
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0)).thenReturn("jwt-token");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        var result = authService.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    public void shouldThrowWhenEmailNotVerified() {
        var request = new LoginRequest("anton@example.com", "password123");
        var user = User.builder()
                .email("anton@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Please verify your email before login");
    }

    @Test
    public void shouldRegister() {
        var request = new RegisterRequest(
                "Anton",
                "anton@example.com",
                "password123",
                "password123",
                "+380961791111",
                null
        );
        var user = User.builder()
                .email("anton@example.com")
                .userRole(UserRole.CUSTOMER)
                .tokenVersion(0)
                .build();
        var userResponse = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", UserRole.CUSTOMER, null);

        when(userService.create(request)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0)).thenReturn("jwt-token");

        var result = authService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        verify(tokenService).createVerificationToken(user);
    }
}