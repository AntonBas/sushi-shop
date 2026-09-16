package com.sushishop.user;

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
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/users/me/password - {}", userDetails.getUsername());
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/google-unlink")
    @Operation(summary = "Disable Google sign-in for the current account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Google sign-in disabled"),
            @ApiResponse(responseCode = "400", description = "No password set, or Google sign-in already disabled")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> disableGoogleSignIn(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/users/me/google-unlink - {}", userDetails.getUsername());
        userService.disableGoogleSignIn(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
