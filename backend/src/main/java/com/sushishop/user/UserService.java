package com.sushishop.user;

import com.sushishop.annotation.Auditable;
import com.sushishop.auth.dto.request.RegisterRequest;
import com.sushishop.mail.MailService;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.enums.TokenType;
import com.sushishop.shared.enums.UserRole;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.dto.request.ChangePasswordRequest;
import com.sushishop.user.dto.request.UpdateUserRequest;
import com.sushishop.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int TOKEN_EXPIRATION_HOURS = 1;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MailService mailService;

    @Auditable(action = AuditAction.CREATE, entity = "User")
    @Transactional
    public UserResponse create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists!");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords don't match!");
        }
        var user = toEntity(request);
        var saved = userRepository.save(user);

        var token = Token.builder().token(UUID.randomUUID().toString()).tokenType(TokenType.EMAIL_VERIFICATION).user(saved).expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS)).build();
        tokenRepository.save(token);

        mailService.sendVerificationEmail(saved.getEmail(), token.getToken());
        log.info("User created: {}", saved.getEmail());
        return userMapper.toResponse(saved);
    }

    @Cacheable(value = "users", key = "#email")
    public UserResponse getByEmail(String email) {
        log.info("Getting user by email: {}", email);
        return userRepository.findByEmail(email).map(userMapper::toResponse).orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    @Auditable(action = AuditAction.UPDATE, entity = "User")
    @CacheEvict(value = "users", key = "#email")
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

    @Auditable(action = AuditAction.UPDATE, entity = "User")
    @Transactional
    @CacheEvict(value = "userCache", key = "#email + ':*'")
    public void changePassword(String email, ChangePasswordRequest request) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found: " + email));

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

    private User toEntity(RegisterRequest request) {
        return User.builder().name(request.name()).email(request.email()).password(passwordEncoder.encode(request.password())).phone(request.phone()).city(request.address() != null ? request.address().city() : null).street(request.address() != null ? request.address().street() : null).house(request.address() != null ? request.address().house() : null).apartment(request.address() != null ? request.address().apartment() : null).userRole(UserRole.CUSTOMER).build();
    }
}