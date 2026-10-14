package com.sushishop.audit;

import com.sushishop.shared.service.TransactionCallbacks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final AuditorAware<String> auditorAware;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result",
            argNames = "joinPoint,auditable,result")
    public void audit(JoinPoint joinPoint, Auditable auditable, Object result) {
        String user = auditorAware.getCurrentAuditor().orElse("system");
        Long entityId = auditable.idParam().isEmpty()
                ? extractId(result)
                : extractIdFromArgument(joinPoint, auditable.idParam());
        String details = auditable.action() + " " + auditable.entity() + (entityId != null ? " #" + entityId : "")
                + (auditable.description().isEmpty() ? "" : ": " + auditable.description());
        TransactionCallbacks.afterCommit(
                () -> auditLogService.log(auditable.action(), auditable.entity(), entityId, details, user));
    }

    private Long extractIdFromArgument(JoinPoint joinPoint, String paramName) {
        var parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        var args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(paramName)) {
                return args[i] instanceof Number number ? number.longValue() : null;
            }
        }
        log.warn("Auditable parameter '{}' not found on {}", paramName, joinPoint.getSignature().toShortString());
        return null;
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
        return Optional.ofNullable(ReflectionUtils.findField(type, "id"));
    }
}