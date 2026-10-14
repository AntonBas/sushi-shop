package com.sushishop.audit;

import com.sushishop.shared.enums.AuditAction;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuditorAware<String> auditorAware;

    @InjectMocks
    private AuditAspect auditAspect;

    private record DirectIdResult(Long id) {
    }

    private static class BaseFixture {
        private final Long id;

        BaseFixture(Long id) {
            this.id = id;
        }
    }

    private static class InheritedIdResult extends BaseFixture {
        InheritedIdResult(Long id) {
            super(id);
        }
    }

    private record NoIdResult(String name) {
    }

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private Auditable auditable(AuditAction action, String entity) {
        return auditable(action, entity, "", "");
    }

    private Auditable auditable(AuditAction action, String entity, String idParam, String description) {
        var mock = org.mockito.Mockito.mock(Auditable.class);
        when(mock.action()).thenReturn(action);
        when(mock.entity()).thenReturn(entity);
        when(mock.idParam()).thenReturn(idParam);
        when(mock.description()).thenReturn(description);
        return mock;
    }

    private void stubArguments(String[] names, Object[] values) {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(names);
        when(joinPoint.getArgs()).thenReturn(values);
    }

    @Test
    void shouldTakeIdFromNamedArgumentWhenMethodReturnsVoid() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));
        stubArguments(new String[]{"reviewId", "email"}, new Object[]{15L, "admin@shop.com"});

        auditAspect.audit(joinPoint, auditable(AuditAction.DELETE, "Review", "reviewId", ""), null);

        verify(auditLogService).log(eq(AuditAction.DELETE), eq("Review"), eq(15L), eq("DELETE Review #15"),
                eq("admin@shop.com"));
    }

    @Test
    void shouldAppendDescriptionToDetails() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());

        auditAspect.audit(joinPoint, auditable(AuditAction.UPDATE, "User", "", "Password reset"), 9L);

        verify(auditLogService).log(eq(AuditAction.UPDATE), eq("User"), eq(9L), eq("UPDATE User #9: Password reset"),
                eq("system"));
    }

    @Test
    void shouldLogNullEntityIdWhenNamedArgumentIsMissing() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));
        stubArguments(new String[]{"id"}, new Object[]{3L});
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        auditAspect.audit(joinPoint, auditable(AuditAction.DELETE, "Product", "productId", ""), null);

        verify(auditLogService).log(eq(AuditAction.DELETE), eq("Product"), isNull(), eq("DELETE Product"),
                eq("admin@shop.com"));
    }

    @Test
    void shouldExtractIdDeclaredDirectlyOnTheResultClass() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(joinPoint, auditable(AuditAction.CREATE, "Product"), new DirectIdResult(42L));

        verify(auditLogService).log(eq(AuditAction.CREATE), eq("Product"), eq(42L), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldExtractIdDeclaredOnASuperclass() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(joinPoint, auditable(AuditAction.UPDATE, "User"), new InheritedIdResult(7L));

        verify(auditLogService).log(eq(AuditAction.UPDATE), eq("User"), eq(7L), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldLogNullEntityIdWithoutThrowingWhenResultHasNoIdField() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(joinPoint, auditable(AuditAction.CREATE, "Review"), new NoIdResult("no id here"));

        verify(auditLogService).log(eq(AuditAction.CREATE), eq("Review"), isNull(), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldLogNullEntityIdWhenResultIsVoid() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(joinPoint, auditable(AuditAction.DELETE, "Product"), null);

        verify(auditLogService).log(eq(AuditAction.DELETE), eq("Product"), isNull(), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldWriteAuditLogOnlyAfterTransactionCommits() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@test.com"));
        var auditable = auditable(AuditAction.UPDATE, "User");

        TransactionSynchronizationManager.initSynchronization();
        try {
            auditAspect.audit(joinPoint, auditable, new DirectIdResult(5L));
            verifyNoInteractions(auditLogService);

            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(auditLogService).log(eq(AuditAction.UPDATE), eq("User"), eq(5L), any(), eq("admin@test.com"));
    }

    @Test
    void shouldNotWriteAuditLogWhenTransactionRollsBack() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@test.com"));
        var auditable = auditable(AuditAction.UPDATE, "User");

        TransactionSynchronizationManager.initSynchronization();
        try {
            auditAspect.audit(joinPoint, auditable, new DirectIdResult(5L));
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verifyNoInteractions(auditLogService);
    }
}
