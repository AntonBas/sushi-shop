package com.sushishop.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoUserSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DemoUserSeeder demoUserSeeder;

    @Test
    void shouldNotSeedWhenDisabled() throws Exception {
        ReflectionTestUtils.setField(demoUserSeeder, "seedDemoUsers", false);

        demoUserSeeder.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldSeedMissingDemoUsersWhenEnabled() throws Exception {
        ReflectionTestUtils.setField(demoUserSeeder, "seedDemoUsers", true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        demoUserSeeder.run(null);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(3)).save(captor.capture());

        var roles = captor.getAllValues().stream().map(User::getUserRole).toList();
        assertThat(roles).containsExactlyInAnyOrder(UserRole.CUSTOMER, UserRole.ADMIN, UserRole.COURIER);
    }

    @Test
    void shouldSkipAlreadyExistingDemoUsers() throws Exception {
        ReflectionTestUtils.setField(demoUserSeeder, "seedDemoUsers", true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        demoUserSeeder.run(null);

        verify(userRepository, never()).save(any());
    }
}
