package com.sushishop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Cookie-based auth is vulnerable to CSRF, and the usual Spring Security
 * double-submit cookie defense doesn't apply here: frontend and backend are
 * on different domains (Vercel/Render), so frontend JS can never read a
 * cookie the backend sets. Instead, this requires a custom header on every
 * mutating request. A plain HTML form can't set custom headers, and cross-site
 * JS can't either unless our CORS config allows its origin (it doesn't) —
 * the header itself doesn't need to be secret.
 */
@Component
public class CsrfHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final String REQUIRED_HEADER = "X-Requested-With";
    private static final String WEBHOOK_PATH = "/api/payments/webhook";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        boolean exempt = SAFE_METHODS.contains(request.getMethod()) || WEBHOOK_PATH.equals(request.getRequestURI());
        if (!exempt && request.getHeader(REQUIRED_HEADER) == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing required header: " + REQUIRED_HEADER);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
