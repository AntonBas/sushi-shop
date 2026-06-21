package com.sushishop.service;

import com.sushishop.domain.User;
import com.sushishop.domain.enums.UserRole;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.exception.core.ConflictException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.UserMapper;
import com.sushishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists!");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .city(request.address() != null ? request.address().city() : null)
                .street(request.address() != null ? request.address().street() : null)
                .house(request.address() != null ? request.address().house() : null)
                .apartment(request.address() != null ? request.address().apartment() : null)
                .userRole(UserRole.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        log.info("User created: {}", saved.getEmail());
        return userMapper.toResponse(saved);
    }

    public UserResponse getById(Long id) {
        return userRepository.findById(id).map(userMapper::toResponse).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toResponse).orElseThrow(() -> new NotFoundException("User not found: " + email));
    }
}
