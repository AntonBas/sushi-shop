package com.sushishop.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Optional;

@Slf4j
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

        var idField = findIdField(result.getClass());
        if (idField.isEmpty()) {
            log.warn("Auditable result of type {} has no 'id' field; logging entityId as null", result.getClass().getSimpleName());
            return null;
        }

        try {
            idField.get().setAccessible(true);
            return idField.get().get(result) instanceof Long longId ? longId : null;
        } catch (IllegalAccessException e) {
            log.warn("Could not read 'id' field of {}", result.getClass().getSimpleName(), e);
            return null;
        }
    }

    private Optional<Field> findIdField(Class<?> type) {
        for (var current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return Optional.of(current.getDeclaredField("id"));
            } catch (NoSuchFieldException ignored) {
                // keep walking up to the next superclass
            }
        }
        return Optional.empty();
    }
}