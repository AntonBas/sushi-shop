package com.sushishop.mapper;

import com.sushishop.domain.AuditLog;
import com.sushishop.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponse toResponse(AuditLog auditLog);
}