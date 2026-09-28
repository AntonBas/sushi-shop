package com.sushishop.audit;

import com.sushishop.user.UserService;
import com.sushishop.user.dto.request.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class AuditRollbackIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldNotWriteAuditLogWhenTransactionFailsAtCommit() {
        var before = countUserUpdates();

        assertThatThrownBy(() -> userService.update("user@test.com", new UpdateUserRequest("", null, null)))
                .isNotNull();

        assertThat(countUserUpdates()).isEqualTo(before);
    }

    @Test
    void shouldWriteAuditLogAfterSuccessfulCommit() {
        var before = countUserUpdates();

        userService.update("user@test.com", new UpdateUserRequest("Anton", null, null));

        assertThat(countUserUpdates()).isEqualTo(before + 1);
    }

    private long countUserUpdates() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_logs WHERE entity_name = 'User' AND action = 'UPDATE'", Long.class);
    }
}
