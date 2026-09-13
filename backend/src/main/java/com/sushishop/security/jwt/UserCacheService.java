package com.sushishop.security.jwt;

import com.sushishop.user.CachedAuthUser;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;

    @SuppressWarnings("unused")
    @Cacheable(value = "userCache", key = "#email + ':' + #tokenVersion", unless = "#result == null")
    public CachedAuthUser getCachedUser(String email, Integer tokenVersion) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getTokenVersion().equals(tokenVersion))
                .map(user -> new CachedAuthUser(user.getEmail(), user.getPassword(), user.getUserRole(), user.isEmailVerified()))
                .orElse(null);
    }
}
