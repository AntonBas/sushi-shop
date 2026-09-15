package com.sushishop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.shared.exception.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * oauth2Login() registers a default AuthenticationEntryPoint that redirects
 * unauthenticated requests to its generated "choose a login provider" page.
 * The client follows that redirect and lands on a 200 OK HTML response, not
 * a 401 — so the frontend's getMe() call "succeeds" with an HTML string as
 * the user object, making an unauthenticated visitor appear logged in. This
 * backend is API-only (the frontend drives OAuth2 login itself via
 * /oauth2/authorization/{id}), so every unauthenticated request here should
 * get a plain 401 instead of that redirect.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                          @NonNull AuthenticationException authException) throws IOException {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "Authentication required");
        apiError.setPath(request.getRequestURI());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
