package com.sushishop.user;

import com.sushishop.audit.Auditable;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.dto.request.ChangePasswordRequest;
import com.sushishop.user.dto.request.RegisterRequest;
import com.sushishop.user.dto.request.UpdateUserRequest;
import com.sushishop.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Auditable(action = AuditAction.CREATE, entity = "User")
    @Transactional
    public User create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists!");
        }

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        var saved = userRepository.save(user);
        log.info("User created: {}", saved.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email")
    public UserResponse getByEmail(String email) {
        log.info("Getting user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    @Auditable(action = AuditAction.UPDATE, entity = "User")
    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public UserResponse update(String email, UpdateUserRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.address() != null) {
            user.setCity(request.address().city());
            user.setStreet(request.address().street());
            user.setHouse(request.address().house());
            user.setApartment(request.address().apartment());
        }

        var saved = userRepository.save(user);
        log.info("User updated: {}", saved.getEmail());
        return userMapper.toResponse(saved);
    }

    @Auditable(action = AuditAction.UPDATE, entity = "User")
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match!");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        log.info("Password changed for {}", email);
    }
}