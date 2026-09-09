package com.sushishop.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
public class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldLoadSeededDemoUsersWithValidRole() {
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
}
