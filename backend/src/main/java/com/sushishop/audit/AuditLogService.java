package com.sushishop.audit;

import com.sushishop.audit.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityName, Long entityId, String details, String performedBy) {
        if (action == null || action.isBlank()) {
            log.warn("Audit log skipped: action is empty");
            return;
        }

        var auditLog = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .performedBy(performedBy != null ? performedBy : "system")
                .build();
        auditLogRepository.save(auditLog);
        log.info("Audit: {} {} [{}] by {}", action, entityName, entityId, performedBy);
    }

    public Page<AuditLogResponse> getAll(String action, String entityName, Long entityId,
                                         String performedBy, LocalDateTime start, LocalDateTime end,
                                         Pageable pageable) {
        var spec = AuditLogSpecification.filter(action, entityName, entityId, performedBy, start, end);
        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toResponse);
    }
}
