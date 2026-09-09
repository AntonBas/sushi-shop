package com.sushishop.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-users:false}")
    private boolean seedDemoUsers;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedDemoUsers) {
            return;
        }
        seedIfMissing("user@test.com", "user", "Test User", "+380991234567", UserRole.CUSTOMER);
        seedIfMissing("admin@test.com", "admin", "Admin", "+380997654321", UserRole.ADMIN);
        seedIfMissing("courier@test.com", "courier", "Courier", "+380995554433", UserRole.COURIER);
    }

    private void seedIfMissing(String email, String rawPassword, String name, String phone, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        var user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .name(name)
                .phone(phone)
                .userRole(role)
                .emailVerified(true)
                .tokenVersion(0)
                .build();
        userRepository.save(user);
        log.info("Seeded demo user: {}", email);
    }
}
