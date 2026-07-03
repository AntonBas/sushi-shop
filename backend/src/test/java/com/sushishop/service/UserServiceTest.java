package com.sushishop.service;

import com.sushishop.domain.User;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.ConflictException;
import com.sushishop.mapper.UserMapper;
import com.sushishop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        var request = new RegisterRequest("Anton", "anton@example.com", "password123", "password123", "+380961791111", null);
        var user = new User();
        var expected = new UserResponse(1L, "Anton", "anton@example.com", "+380961791111", "CUSTOMER", null);

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(expected);

        var result = userService.create(request);

        assertThat(result.email()).isEqualTo("anton@example.com");
        verify(userRepository).save(any());
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
}