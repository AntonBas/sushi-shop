package com.sushishop.controller;

import com.sushishop.dto.request.CreatePromotionRequest;
import com.sushishop.dto.response.PromotionResponse;
import com.sushishop.service.PromotionService;
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
    @Operation(summary = "Create promotion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Promotion created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody CreatePromotionRequest request) {
        log.info("POST /api/promotions - {}", request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(request));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active promotions")
    public ResponseEntity<List<PromotionResponse>> getActive() {
        log.info("GET /api/promotions/active");
        return ResponseEntity.ok(promotionService.getActive());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all promotions (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<PromotionResponse>> getAll(@PageableDefault(size = 12, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/promotions");
        return ResponseEntity.ok(promotionService.getAll(pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete promotion")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/promotions/{}", id);
        promotionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}