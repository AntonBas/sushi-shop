package com.sushishop.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private final OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://frontend.example.com");
        when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldRedirectWithOAuth2ErrorCode() throws Exception {
        var exception = new OAuth2AuthenticationException(new OAuth2Error("access_denied"), "User denied access");

        handler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect(eq("https://frontend.example.com/login?error=access_denied"));
    }

    @Test
    void shouldRedirectWithUnverifiedEmailErrorCode() throws Exception {
        var exception = new OAuth2AuthenticationException(
                new OAuth2Error("unverified_email", "Google account email is not verified", null),
                "unverified_email");

        handler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect(eq("https://frontend.example.com/login?error=unverified_email"));
    }

    @Test
    void shouldFallBackToDefaultErrorCodeForNonOAuth2Exception() throws Exception {
        var exception = new BadCredentialsException("bad credentials");

        handler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect(eq("https://frontend.example.com/login?error=oauth2_failed"));
    }
}
