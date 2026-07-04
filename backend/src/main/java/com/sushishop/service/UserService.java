package com.sushishop.service;

import com.sushishop.domain.User;
import com.sushishop.domain.enums.UserRole;
import com.sushishop.dto.request.ChangePasswordRequest;
import com.sushishop.dto.request.RegisterRequest;
import com.sushishop.dto.request.UpdateUserRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.ConflictException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.UserMapper;
import com.sushishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MailService mailService;

    public UserResponse create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists!");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords don't match!");
        }
        var user = toEntity(request);
        user.setVerificationToken(UUID.randomUUID().toString());
        var saved = userRepository.save(user);
        mailService.sendVerificationEmail(saved.getEmail(), saved.getVerificationToken());
        log.info("User created: {}", saved.getEmail());
        return userMapper.toResponse(saved);
    }

    public UserResponse getByEmail(String email) {
        log.info("Getting user by email: {}", email);
        return userRepository.findByEmail(email).map(userMapper::toResponse).orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    public UserResponse update(String email, UpdateUserRequest request) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (request.name() != null) user.setName(request.name());
        if (request.phone() != null) user.setPhone(request.phone());
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

    public void changePassword(String email, ChangePasswordRequest request) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match!");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for {}", email);
    }

    private User toEntity(RegisterRequest request) {
        return User.builder().name(request.name()).email(request.email()).password(passwordEncoder.encode(request.password())).phone(request.phone()).city(request.address() != null ? request.address().city() : null).street(request.address() != null ? request.address().street() : null).house(request.address() != null ? request.address().house() : null).apartment(request.address() != null ? request.address().apartment() : null).userRole(UserRole.CUSTOMER).build();
    }
}
