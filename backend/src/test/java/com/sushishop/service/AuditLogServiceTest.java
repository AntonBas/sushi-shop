package com.sushishop.service;

import com.sushishop.domain.AuditLog;
import com.sushishop.dto.response.AuditLogResponse;
import com.sushishop.mapper.AuditLogMapper;
import com.sushishop.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
        auditLogService.log("CREATE", "Product", 1L, "Product created", "admin@example.com");
        verify(auditLogRepository).save(any());
    }

    @Test
    void shouldGetAll() {
        var log = new AuditLog();
        var response = new AuditLogResponse(1L, "CREATE", "Product", 5L, "details", "admin@example.com", null);
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toResponse(log)).thenReturn(response);

        var result = auditLogService.getAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }
}