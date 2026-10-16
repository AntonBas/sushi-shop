package com.sushishop.admin;

import com.sushishop.admin.dto.AdminUserResponse;
import com.sushishop.audit.Auditable;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.user.UserSessionsInvalidatedEvent;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserQueryRepository adminUserQueryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(AdminUserFilter filter, Pageable pageable) {
        return adminUserQueryRepository.search(filter, pageable);
    }

    @Auditable(action = AuditAction.UPDATE, entity = "User", idParam = "id", description = "Role changed")
    @Transactional
    public void changeRole(Long id, UserRole role, String adminEmail) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        if (user.getEmail().equals(adminEmail)) {
            throw new BadRequestException("You cannot change your own role", "CANNOT_CHANGE_OWN_ROLE");
        }
        if (user.getUserRole() == role) {
            return;
        }

        var previousRole = user.getUserRole();
        var previousTokenVersion = user.getTokenVersion();
        user.setUserRole(role);
        user.setTokenVersion(previousTokenVersion + 1);
        userRepository.save(user);

        eventPublisher.publishEvent(new UserSessionsInvalidatedEvent(this, user.getEmail(), previousTokenVersion));
        log.info("User {} role changed from {} to {} by {}", id, previousRole, role, adminEmail);
    }
}
