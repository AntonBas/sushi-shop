package com.sushishop.audit;

import com.sushishop.audit.dto.AuditLogResponse;
import com.sushishop.shared.enums.AuditAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void shouldLogAction() {
        auditLogService.log(AuditAction.CREATE, "Product", 1L, "Product created", "admin@example.com");
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldNotLogWhenActionIsNull() {
        auditLogService.log(null, "Product", 1L, "details", "admin@example.com");
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void shouldUseSystemAsDefaultPerformedBy() {
        auditLogService.log(AuditAction.CREATE, "Product", 1L, "details", null);
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldGetAllWithNoFilters() {
        var log = new AuditLog();
        var response = new AuditLogResponse(1L, AuditAction.CREATE, "Product", 5L, "details", "admin@example.com", LocalDateTime.now());
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogRepository.findAll(ArgumentMatchers.<Specification<AuditLog>>any(), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toResponse(log)).thenReturn(response);

        var result = auditLogService.getAll(null, null, null, null, null, null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldGetAllWithFilters() {
        var log = new AuditLog();
        var response = new AuditLogResponse(1L, AuditAction.CREATE, "Product", 5L, "details", "admin@example.com", LocalDateTime.now());
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogRepository.findAll(ArgumentMatchers.<Specification<AuditLog>>any(), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toResponse(log)).thenReturn(response);

        var result = auditLogService.getAll(AuditAction.CREATE, "Product", 5L, "admin@example.com",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }
}