package com.sushishop.security.oauth2;

import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2ExchangeCodeService exchangeCodeService;

    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(userRepository, exchangeCodeService);
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://frontend.example.com");
    }

    @Test
    void shouldRedirectToFrontendWithOneTimeCodeForNormalizedEmail() throws Exception {
        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(new User()));
        when(exchangeCodeService.issueCode("anton@example.com")).thenReturn("code-123");
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authenticationFor("Anton@Example.com"));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://frontend.example.com/oauth2/redirect?code=code-123");
    }

    @Test
    void shouldNotIssueCodeWhenUserIsMissing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        var authentication = authenticationFor("ghost@example.com");

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), new MockHttpServletResponse(), authentication))
                .isInstanceOf(IllegalStateException.class);
        verify(exchangeCodeService, never()).issueCode(anyString());
    }

    private static Authentication authenticationFor(String email) {
        var principal = new DefaultOAuth2User(List.of(), Map.of("email", email, "sub", "google-id"), "sub");
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        return authentication;
    }
}
