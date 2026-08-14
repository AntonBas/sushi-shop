package com.sushishop.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final AuditorAware<String> auditorAware;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void audit(Auditable auditable, Object result) {
        String user = auditorAware.getCurrentAuditor().orElse("system");
        Long entityId = extractId(result);
        String details = auditable.action() + " " + auditable.entity() + (entityId != null ? " #" + entityId : "");
        auditLogService.log(auditable.action(), auditable.entity(), entityId, details, user);
    }

    private Long extractId(Object result) {
        if (result == null) return null;
        try {
            Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            if (id instanceof Long longId) return longId;
        } catch (NoSuchMethodException e) {
            try {
                Method idMethod = result.getClass().getMethod("id");
                Object id = idMethod.invoke(result);
                if (id instanceof Long longId) return longId;
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}