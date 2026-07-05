package com.sushishop.service;

import com.sushishop.domain.AuditLog;
import com.sushishop.dto.response.AuditLogResponse;
import com.sushishop.mapper.AuditLogMapper;
import com.sushishop.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public void log(String action, String entityName, Long entityId, String details, String performedBy) {
        var auditLog = AuditLog.builder().action(action).entityName(entityName).entityId(entityId).details(details).performedBy(performedBy).performedAt(LocalDateTime.now()).build();
        auditLogRepository.save(auditLog);
        log.info("Audit: {} {} [{}] by {}", action, entityName, entityId, performedBy);
    }

    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
    }
}
