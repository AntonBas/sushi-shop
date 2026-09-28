package com.sushishop.security.oauth2;

import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private static OAuth2User googleUser(String email, Boolean emailVerified) {
        return new DefaultOAuth2User(
                List.of(() -> "ROLE_USER"),
                Map.of("email", email, "name", "Test User", "email_verified", emailVerified, "sub", "google-id"),
                "sub");
    }

    @Test
    void shouldRejectUnverifiedGoogleEmail() {
        var oauthUser = googleUser("attacker@gmail.com", false);

        assertThatThrownBy(() -> customOAuth2UserService.linkOrCreateUser(oauthUser))
                .isInstanceOf(OAuth2AuthenticationException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void shouldCreateNewVerifiedUserWhenNoneExists() {
        var oauthUser = googleUser("new@gmail.com", true);
        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOAuth2UserService.linkOrCreateUser(oauthUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("new@gmail.com");
        assertThat(captor.getValue().isEmailVerified()).isTrue();
        assertThat(captor.getValue().getPassword()).isNull();
    }

    @Test
    void shouldClearPasswordAndBumpTokenVersionWhenClaimingUnverifiedExistingAccount() {
        var attackerControlledUser = User.builder()
                .email("victim@gmail.com")
                .name("Victim")
                .emailVerified(false)
                .password("attacker-set-bcrypt-hash")
                .tokenVersion(3)
                .build();
        var oauthUser = googleUser("victim@gmail.com", true);
        when(userRepository.findByEmail("victim@gmail.com")).thenReturn(Optional.of(attackerControlledUser));

        customOAuth2UserService.linkOrCreateUser(oauthUser);

        assertThat(attackerControlledUser.isEmailVerified()).isTrue();
        assertThat(attackerControlledUser.getPassword()).isNull();
        assertThat(attackerControlledUser.getTokenVersion()).isEqualTo(4);
        verify(userRepository).save(attackerControlledUser);

        var eventCaptor = ArgumentCaptor.forClass(UserSessionsInvalidatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEmail()).isEqualTo("victim@gmail.com");
        assertThat(eventCaptor.getValue().getPreviousTokenVersion()).isEqualTo(3);
    }

    @Test
    void shouldNotTouchPasswordOrTokenVersionForAlreadyVerifiedUser() {
        var existingUser = User.builder()
                .email("returning@gmail.com")
                .name("Returning User")
                .emailVerified(true)
                .password("still-their-own-password")
                .tokenVersion(1)
                .build();
        var oauthUser = googleUser("returning@gmail.com", true);
        when(userRepository.findByEmail("returning@gmail.com")).thenReturn(Optional.of(existingUser));

        customOAuth2UserService.linkOrCreateUser(oauthUser);

        assertThat(existingUser.getPassword()).isEqualTo("still-their-own-password");
        assertThat(existingUser.getTokenVersion()).isEqualTo(1);
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
