package com.sushishop.controller;

import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.dto.request.CreateOrderRequest;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.dto.response.UserOrderResponse;
import com.sushishop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/orders - {}", request.customerName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user orders")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UserOrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/orders/my - user: {}", userDetails.getUsername());
        return ResponseEntity.ok(orderService.getByUser(userDetails.getUsername(), pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<OrderResponse>> getAll(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/orders (admin)");
        return ResponseEntity.ok(orderService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        log.info("GET /api/orders/{} - by ID", id);
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        log.info("PATCH /api/orders/{}/status - {}", id, status);
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}