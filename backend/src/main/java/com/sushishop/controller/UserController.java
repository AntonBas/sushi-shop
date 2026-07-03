package com.sushishop.controller;

import com.sushishop.dto.request.UpdateUserRequest;
import com.sushishop.dto.response.UserResponse;
import com.sushishop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
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
}
