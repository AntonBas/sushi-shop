package com.sushishop.user;

import com.sushishop.shared.ratelimit.RateLimit;
import com.sushishop.user.dto.request.ChangePasswordRequest;
import com.sushishop.user.dto.request.UpdateUserRequest;
import com.sushishop.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User manager endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/users/me - {}", userDetails.getUsername());
        return ResponseEntity.ok(userService.getByEmail(userDetails.getUsername()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UpdateUserRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/users/me - {}", userDetails.getUsername());
        return ResponseEntity.ok(userService.update(userDetails.getUsername(), request));
    }

    @DeleteMapping("/me/address")
    @Operation(summary = "Remove the default delivery address")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> clearAddress(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/users/me/address - {}", userDetails.getUsername());
        return ResponseEntity.ok(userService.clearAddress(userDetails.getUsername()));
    }

    @RateLimit(duration = 900, key = "user")
    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/users/me/password - {}", userDetails.getUsername());
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
