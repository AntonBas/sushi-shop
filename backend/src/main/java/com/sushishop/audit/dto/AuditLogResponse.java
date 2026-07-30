package com.sushishop.audit.dto;

import com.sushishop.shared.enums.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Audit log response")
public record AuditLogResponse(
        @Schema(description = "Log ID")
        Long id,

        @Schema(description = "Action performed")
        AuditAction action,

        @Schema(description = "Entity name")
        String entityName,

        @Schema(description = "Entity ID")
        Long entityId,

        @Schema(description = "Details")
        String details,

        @Schema(description = "Performed by")
        String performedBy,

        @Schema(description = "Performed at")
        LocalDateTime performedAt
) {
}