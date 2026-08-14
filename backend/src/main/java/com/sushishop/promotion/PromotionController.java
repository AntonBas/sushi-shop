package com.sushishop.promotion;

import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.request.UpdatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Promotion management endpoints")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create promotion", description = "Admin only. Creates a new promotion with validated products and dates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Promotion created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or product conflict"),
            @ApiResponse(responseCode = "404", description = "Products not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody CreatePromotionRequest request) {
        var response = promotionService.create(request);
        log.info("Promotion created: id={}, title={}", response.id(), response.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active promotions", description = "Public endpoint. Returns all currently active promotions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active promotions")
    })
    public ResponseEntity<List<PromotionResponse>> getActive() {
        return ResponseEntity.ok(promotionService.getActive());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all promotions", description = "Admin only. Returns paginated list with optional search.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of promotions")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<PromotionResponse>> getAll(
            @PageableDefault(size = 12, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(promotionService.getAll(pageable, search));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get promotion by slug", description = "Public endpoint. Returns promotion details by slug for storefront.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion found"),
            @ApiResponse(responseCode = "404", description = "Promotion not found")
    })
    public ResponseEntity<PromotionResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(promotionService.getBySlug(slug));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get promotion by ID", description = "Admin only. Used for editing and management.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Promotion not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update promotion", description = "Admin only. Supports partial updates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promotion updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input or product conflict"),
            @ApiResponse(responseCode = "404", description = "Promotion not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PromotionResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePromotionRequest request) {
        var response = promotionService.update(id, request);
        log.info("Promotion updated: id={}, title={}", response.id(), response.title());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete promotion", description = "Admin only. Removes promotion and clears product associations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Promotion deleted"),
            @ApiResponse(responseCode = "404", description = "Promotion not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        log.info("Promotion deleted: id={}", id);
        return ResponseEntity.noContent().build();
    }
}