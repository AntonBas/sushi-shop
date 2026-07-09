package com.sushishop.controller;

import com.sushishop.dto.response.AuditLogResponse;
import com.sushishop.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class AuditLogControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAllAuditLogs() throws Exception {
        var log = new AuditLogResponse(1L, "CREATE", "Product", 1L, "Product created", "admin@example.com", LocalDateTime.now());
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log));

        when(auditLogService.getAll(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/audit"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAuditLogsWithFilters() throws Exception {
        var log = new AuditLogResponse(1L, "CREATE", "Product", 1L, "Product created", "admin@example.com", LocalDateTime.now());
        Page<AuditLogResponse> page = new PageImpl<>(List.of(log));

        when(auditLogService.getAll(eq("CREATE"), eq("Product"), eq(1L), eq("admin@example.com"), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/audit")
                        .param("action", "CREATE")
                        .param("entityName", "Product")
                        .param("entityId", "1")
                        .param("performedBy", "admin@example.com"))
                .andExpect(status().isOk());
    }
}