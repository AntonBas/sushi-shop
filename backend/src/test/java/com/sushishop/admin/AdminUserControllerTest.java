package com.sushishop.admin;

import com.sushishop.admin.dto.response.AdminUserResponse;
import com.sushishop.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldReturnUsersWithFilters() throws Exception {
        var user = new AdminUserResponse(2L, "Anna", "anna@example.com", null, UserRole.CUSTOMER,
                true, true, null, 3, new BigDecimal("500.00"));
        when(adminUserService.getUsers(any(), any())).thenReturn(new PageImpl<>(List.of(user), Pageable.ofSize(20), 1));

        mockMvc.perform(get("/api/admin/users")
                        .param("search", "anna")
                        .param("role", "CUSTOMER")
                        .param("emailVerified", "true")
                        .param("sort", "totalSpent,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ordersCount").value(3))
                .andExpect(jsonPath("$.content[0].totalSpent").value(500.00));

        verify(adminUserService).getUsers(eq(new AdminUserFilter("anna", UserRole.CUSTOMER, true)),
                argThat(pageable -> pageable.getSort().getOrderFor("totalSpent") != null));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectUnknownSortField() throws Exception {
        mockMvc.perform(get("/api/admin/users").param("sort", "password,asc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminUserService);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldChangeRole() throws Exception {
        mockMvc.perform(patch("/api/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"COURIER\"}"))
                .andExpect(status().isNoContent());

        verify(adminUserService).changeRole(2L, UserRole.COURIER, "admin@example.com");
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectMissingRole() throws Exception {
        mockMvc.perform(patch("/api/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminUserService);
    }

    @Test
    @WithMockUser(username = "courier@example.com", roles = {"COURIER"})
    void shouldForbidNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminUserService);
    }
}
