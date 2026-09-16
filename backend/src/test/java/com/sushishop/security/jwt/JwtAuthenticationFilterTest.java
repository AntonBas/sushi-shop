package com.sushishop.security.jwt;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private JwtCookieService jwtCookieService;

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldClearContextAndSkipUserLookupWhenTokenIsBlacklisted() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var payload = new JwtUtil.JwtPayload("anton@example.com", 0, "jti-123", new Date());

        when(jwtCookieService.extractToken(request)).thenReturn("token123");
        when(jwtUtil.parseToken("token123")).thenReturn(payload);
        when(jwtBlacklistService.isBlacklisted("jti-123")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userCacheService, never()).getCachedUser(any(), any());
        verify(filterChain).doFilter(request, response);
    }
}
