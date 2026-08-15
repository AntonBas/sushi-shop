package com.sushishop.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final AuditorAware<String> auditorAware;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result", argNames = "auditable,result")
    public void audit(Auditable auditable, Object result) {
        String user = auditorAware.getCurrentAuditor().orElse("system");
        Long entityId = extractId(result);
        String details = auditable.action() + " " + auditable.entity() + (entityId != null ? " #" + entityId : "");
        auditLogService.log(auditable.action(), auditable.entity(), entityId, details, user);
    }

    private Long extractId(Object result) {
        if (result == null) return null;

        if (result instanceof Number number) {
            return number.longValue();
        }

        try {
            var idField = result.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            var id = idField.get(result);
            if (id instanceof Long longId) {
                return longId;
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return null;
    }
}