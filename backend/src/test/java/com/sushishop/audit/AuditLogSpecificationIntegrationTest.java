package com.sushishop.audit;

import com.sushishop.shared.enums.AuditAction;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class AuditLogSpecificationIntegrationTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 10, 12, 0);

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM audit_logs");
        insert("Order", 1L, "admin@test.com", AuditAction.UPDATE, BASE);
        insert("Order", 2L, "courier@test.com", AuditAction.UPDATE, BASE.plusDays(1));
        insert("Product", 1L, "admin@test.com", AuditAction.CREATE, BASE.plusDays(2));
        insert("Product", 3L, "admin@test.com", AuditAction.DELETE, BASE.plusDays(3));
    }

    @Test
    void shouldReturnAllLogsWhenNoFiltersGiven() {
        assertThat(find(null, null, null, null, null, null)).hasSize(4);
    }

    @Test
    void shouldFilterByAction() {
        assertThat(find(AuditAction.UPDATE, null, null, null, null, null))
                .extracting(AuditLog::getEntityId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldFilterByEntityNameIgnoringCaseAndBlank() {
        assertThat(find(null, "product", null, null, null, null)).hasSize(2);
        assertThat(find(null, "  ", null, null, null, null)).hasSize(4);
    }

    @Test
    void shouldFilterByEntityNameAndId() {
        assertThat(find(null, "Product", 1L, null, null, null))
                .singleElement()
                .extracting(AuditLog::getAction)
                .isEqualTo(AuditAction.CREATE);
    }

    @Test
    void shouldFilterByPerformedByIgnoringCase() {
        assertThat(find(null, null, null, "COURIER@test.com", null, null))
                .singleElement()
                .extracting(AuditLog::getEntityId)
                .isEqualTo(2L);
    }

    @Test
    void shouldFilterByInclusiveDateRange() {
        assertThat(find(null, null, null, null, BASE.plusDays(1), BASE.plusDays(2)))
                .extracting(AuditLog::getPerformedAt)
                .containsExactlyInAnyOrder(BASE.plusDays(1), BASE.plusDays(2));
    }

    @Test
    void shouldCombineAllFilters() {
        assertThat(find(AuditAction.DELETE, "product", 3L, "admin@test.com", BASE, BASE.plusDays(5))).hasSize(1);
        assertThat(find(AuditAction.DELETE, "product", 3L, "courier@test.com", BASE, BASE.plusDays(5))).isEmpty();
    }

    private List<AuditLog> find(AuditAction action, String entityName, Long entityId,
                                String performedBy, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findAll(
                AuditLogSpecification.filter(action, entityName, entityId, performedBy, start, end));
    }

    private void insert(String entityName, Long entityId, String performedBy, AuditAction action, LocalDateTime performedAt) {
        jdbcTemplate.update("""
                INSERT INTO audit_logs (entity_name, entity_id, performed_by, action, performed_at)
                VALUES (?, ?, ?, ?, ?)
                """, entityName, entityId, performedBy, action.name(), performedAt);
    }
}
