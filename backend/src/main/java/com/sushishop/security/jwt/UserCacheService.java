package com.sushishop.security.jwt;

import com.sushishop.user.UserMapper;
import com.sushishop.user.UserRepository;
import com.sushishop.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @SuppressWarnings("unused")
    @Cacheable(value = "userCache", key = "#email + ':' + #tokenVersion", unless = "#result == null")
    public UserResponse getCachedUser(String email, Integer tokenVersion) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getTokenVersion().equals(tokenVersion))
                .map(userMapper::toResponse)
                .orElse(null);
    }
}