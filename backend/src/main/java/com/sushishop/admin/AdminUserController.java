package com.sushishop.admin;

import com.sushishop.admin.dto.ChangeUserRoleRequest;
import com.sushishop.admin.dto.AdminUserResponse;
import com.sushishop.security.Roles;
import com.sushishop.shared.web.SortableFields;
import com.sushishop.user.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('" + Roles.ADMIN + "')")
@Tag(name = "Admin users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Get users with order statistics")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean emailVerified,
            @SortableFields({"createdAt", "name", "email", "ordersCount", "totalSpent"})
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(new AdminUserFilter(search, role, emailVerified), pageable));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change user role")
    @ApiResponse(responseCode = "204", description = "Role changed, user sessions invalidated")
    public ResponseEntity<Void> changeRole(@PathVariable Long id,
                                           @Valid @RequestBody ChangeUserRoleRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PATCH /api/admin/users/{}/role - {}", id, request.role());
        adminUserService.changeRole(id, request.role(), userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
