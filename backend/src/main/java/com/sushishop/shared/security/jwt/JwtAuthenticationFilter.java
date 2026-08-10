package com.sushishop.shared.security.jwt;

import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import com.sushishop.shared.security.user.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmail(token);
                Integer tokenVersion = jwtUtil.getTokenVersion(token);

                User user = getCachedUser(email, tokenVersion);

                if (user != null) {
                    var principal = new CustomUserDetails(user);
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private User getCachedUser(String email, Integer tokenVersion) {
        Cache cache = cacheManager.getCache("userCache");
        if (cache == null) {
            return getUserFromDb(email);
        }

        String cacheKey = email + ":" + tokenVersion;
        User cached = cache.get(cacheKey, User.class);

        if (cached != null) {
            return cached;
        }

        User user = getUserFromDb(email);
        if (user != null) {
            cache.put(cacheKey, user);
        }
        return user;
    }

    private User getUserFromDb(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}