package com.sushishop.service;

import com.sushishop.domain.User;
import com.sushishop.domain.enums.UserRole;
import com.sushishop.dto.request.ChangePasswordRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.ConflictException;
import com.sushishop.mapper.UserMapper;
import com.sushishop.repository.TokenRepository;
import com.sushishop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MailService mailService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);
        var user = User.builder().email("anton@example.com").build();
        var expected = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", UserRole.CUSTOMER, null);

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(expected);

        var result = userService.create(request);

        assertThat(result.email()).isEqualTo("anton@example.com");
        verify(userRepository).save(any());
        verify(tokenRepository).save(any());
        verify(mailService).sendVerificationEmail(eq("anton@example.com"), any());
    }

    @Test
    void shouldThrowWhenEmailExists() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowWhenPasswordsDoNotMatch() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "different", "+380961791111", null);

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldChangePassword() {
        var request = new ChangePasswordRequest("oldPass", "newPass123");
        var user = User.builder().email("anton@example.com").password("hashedOld").build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedNew");

        userService.changePassword("anton@example.com", request);

        assertThat(user.getPassword()).isEqualTo("hashedNew");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenOldPasswordIncorrect() {
        var request = new ChangePasswordRequest("wrongOld", "newPass123");
        var user = User.builder().email("anton@example.com").password("hashedOld").build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "hashedOld")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("anton@example.com", request))
                .isInstanceOf(BadRequestException.class);
    }
}