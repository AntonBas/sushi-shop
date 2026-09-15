package com.sushishop.audit;

import com.sushishop.shared.enums.AuditAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
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

    private Auditable auditable(AuditAction action, String entity) {
        var mock = org.mockito.Mockito.mock(Auditable.class);
        when(mock.action()).thenReturn(action);
        when(mock.entity()).thenReturn(entity);
        return mock;
    }

    @Test
    void shouldExtractIdDeclaredDirectlyOnTheResultClass() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(auditable(AuditAction.CREATE, "Product"), new DirectIdResult(42L));

        verify(auditLogService).log(eq(AuditAction.CREATE), eq("Product"), eq(42L), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldExtractIdDeclaredOnASuperclass() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(auditable(AuditAction.UPDATE, "User"), new InheritedIdResult(7L));

        verify(auditLogService).log(eq(AuditAction.UPDATE), eq("User"), eq(7L), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldLogNullEntityIdWithoutThrowingWhenResultHasNoIdField() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(auditable(AuditAction.CREATE, "Review"), new NoIdResult("no id here"));

        verify(auditLogService).log(eq(AuditAction.CREATE), eq("Review"), isNull(), any(), eq("admin@shop.com"));
    }

    @Test
    void shouldLogNullEntityIdWhenResultIsVoid() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("admin@shop.com"));

        auditAspect.audit(auditable(AuditAction.DELETE, "Product"), null);

        verify(auditLogService).log(eq(AuditAction.DELETE), eq("Product"), isNull(), any(), eq("admin@shop.com"));
    }
}
