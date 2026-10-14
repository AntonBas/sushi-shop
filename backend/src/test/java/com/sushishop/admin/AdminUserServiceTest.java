package com.sushishop.admin;

import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    private static final String ADMIN_EMAIL = "admin@example.com";

    @Mock
    private AdminUserQueryRepository adminUserQueryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void shouldChangeRoleAndInvalidateSessions() {
        var user = user("courier@example.com", UserRole.CUSTOMER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        adminUserService.changeRole(2L, UserRole.COURIER, ADMIN_EMAIL);

        assertThat(user.getUserRole()).isEqualTo(UserRole.COURIER);
        assertThat(user.getTokenVersion()).isEqualTo(4);
        verify(userRepository).save(user);
        var event = ArgumentCaptor.forClass(UserSessionsInvalidatedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().getEmail()).isEqualTo("courier@example.com");
        assertThat(event.getValue().getPreviousTokenVersion()).isEqualTo(3);
    }

    @Test
    void shouldRejectChangingOwnRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(ADMIN_EMAIL, UserRole.ADMIN)));

        assertThatThrownBy(() -> adminUserService.changeRole(1L, UserRole.CUSTOMER, ADMIN_EMAIL))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldKeepSessionsWhenRoleIsUnchanged() {
        var user = user("courier@example.com", UserRole.COURIER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        adminUserService.changeRole(2L, UserRole.COURIER, ADMIN_EMAIL);

        assertThat(user.getTokenVersion()).isEqualTo(3);
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.changeRole(99L, UserRole.ADMIN, ADMIN_EMAIL))
                .isInstanceOf(NotFoundException.class);
    }

    private User user(String email, UserRole role) {
        return User.builder().id(2L).email(email).userRole(role).tokenVersion(3).build();
    }
}
