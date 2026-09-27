package com.sushishop.user;

import com.sushishop.token.TokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldLoadSeededDemoUsersWithValidRole() {
        assertThat(userRepository.findByEmail("user@test.com"))
                .isPresent()
                .get()
                .extracting(User::getUserRole)
                .isEqualTo(UserRole.CUSTOMER);

        assertThat(userRepository.findByEmail("admin@test.com"))
                .isPresent()
                .get()
                .extracting(User::getUserRole)
                .isEqualTo(UserRole.ADMIN);

        assertThat(userRepository.findByEmail("courier@test.com"))
                .isPresent()
                .get()
                .extracting(User::getUserRole)
                .isEqualTo(UserRole.COURIER);
    }

    @Test
    void shouldDeleteOnlyUnverifiedUsersWithoutOrdersOrReviews() {
        long withOrder = insertUnverifiedUser("with-order@example.com");
        insertUnverifiedUser("clean@example.com");
        jdbcTemplate.update("""
                INSERT INTO orders (user_id, customer_name, phone, status, payment_method, delivery_method,
                                    total_amount, created_at)
                VALUES (?, 'Anton', '+380961791111', 'NEW', 'ON_DELIVERY', 'PICKUP', 100, NOW())
                """, withOrder);

        var cutoff = LocalDateTime.now().minusHours(48);
        transactionTemplate.executeWithoutResult(status -> {
            tokenRepository.deleteAllOfUnverifiedUsersWithoutActivityCreatedBefore(cutoff);
            userRepository.deleteUnverifiedWithoutActivityCreatedBefore(cutoff);
        });

        assertThat(userRepository.findByEmail("with-order@example.com")).isPresent();
        assertThat(userRepository.findByEmail("clean@example.com")).isEmpty();
    }

    private long insertUnverifiedUser(String email) {
        jdbcTemplate.update("""
                INSERT INTO users (email, name, phone, user_role, email_verified, token_version, created_at)
                VALUES (?, 'Anton', '+380961791111', 'CUSTOMER', false, 0, NOW() - INTERVAL '3 days')
                """, email);
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }
}
