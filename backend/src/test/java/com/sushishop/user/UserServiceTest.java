package com.sushishop.user;

import com.sushishop.shared.enums.UserRole;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.user.dto.request.ChangePasswordRequest;
import com.sushishop.user.dto.request.RegisterRequest;
import com.sushishop.user.dto.response.UserResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void shouldCreateUser() {
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
                .build();

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);

        var result = userService.create(request);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    public void shouldThrowWhenEmailExists() {
        var request = new RegisterRequest(
                "Anton",
                "anton@example.com",
                "password123",
                "password123",
                "+380961791111",
                null
        );

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    public void shouldThrowWhenPasswordsDoNotMatch() {
        var request = new RegisterRequest(
                "Anton",
                "anton@example.com",
                "password123",
                "different",
                "+380961791111",
                null
        );

        when(userRepository.existsByEmail("anton@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldGetByEmail() {
        var user = User.builder()
                .id(1L)
                .name("Anton")
                .email("anton@example.com")
                .phone("+380961791111")
                .userRole(UserRole.CUSTOMER)
                .build();

        var expected = new UserResponse(
                1L,
                "Anton",
                "anton@example.com",
                "+380961791111",
                UserRole.CUSTOMER,
                null
        );

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        var result = userService.getByEmail("anton@example.com");

        assertThat(result.email()).isEqualTo("anton@example.com");
        verify(userMapper).toResponse(user);
    }

    @Test
    public void shouldUpdateUser() {
        var user = User.builder()
                .id(1L)
                .name("Old Name")
                .email("anton@example.com")
                .phone("+380961791111")
                .build();

        var request = new com.sushishop.user.dto.request.UpdateUserRequest(
                "New Name",
                "+380999999999",
                null
        );

        var expected = new UserResponse(
                1L,
                "New Name",
                "anton@example.com",
                "+380999999999",
                UserRole.CUSTOMER,
                null
        );

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expected);

        var result = userService.update("anton@example.com", request);

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.phone()).isEqualTo("+380999999999");
        verify(userRepository).save(user);
    }

    @Test
    public void shouldChangePassword() {
        var request = new ChangePasswordRequest("oldPass", "newPass123");
        var user = User.builder()
                .email("anton@example.com")
                .password("hashedOld")
                .tokenVersion(0)
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashedOld")).thenReturn(true);
        when(passwordEncoder.matches("newPass123", "hashedOld")).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedNew");

        userService.changePassword("anton@example.com", request);

        assertThat(user.getPassword()).isEqualTo("hashedNew");
        assertThat(user.getTokenVersion()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    public void shouldThrowWhenOldPasswordIncorrect() {
        var request = new ChangePasswordRequest("wrongOld", "newPass123");
        var user = User.builder()
                .email("anton@example.com")
                .password("hashedOld")
                .build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "hashedOld")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("anton@example.com", request))
                .isInstanceOf(BadRequestException.class);
    }
}