package com.sushishop.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Audit log entry")
public record AuditLogResponse(
        @Schema(description = "Log ID", example = "1")
        Long id,

        @Schema(description = "Action performed", example = "CREATE")
        String action,

        @Schema(description = "Entity name", example = "Product")
        String entityName,

        @Schema(description = "Entity ID", example = "5")
        Long entityId,

        @Schema(description = "Details", example = "Product created: Maki")
        String details,

        @Schema(description = "Performed by", example = "admin@example.com")
        String performedBy,

        @Schema(description = "Timestamp")
        LocalDateTime performedAt
) {
}