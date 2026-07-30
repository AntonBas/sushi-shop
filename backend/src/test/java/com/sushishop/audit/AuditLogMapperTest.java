package com.sushishop.audit;

import com.sushishop.shared.enums.AuditAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AuditLogMapperTest {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Test
    void shouldMapToResponse() {
        var log = AuditLog.builder()
                .id(1L)
                .action(AuditAction.CREATE)
                .entityName("Product")
                .entityId(5L)
                .details("Product created: Maki")
                .performedBy("admin@example.com")
                .performedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        var response = auditLogMapper.toResponse(log);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.action()).isEqualTo(AuditAction.CREATE);
        assertThat(response.entityName()).isEqualTo("Product");
        assertThat(response.entityId()).isEqualTo(5L);
        assertThat(response.details()).isEqualTo("Product created: Maki");
        assertThat(response.performedBy()).isEqualTo("admin@example.com");
    }
}