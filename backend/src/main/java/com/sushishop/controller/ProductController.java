package com.sushishop.controller;

import com.sushishop.domain.enums.Category;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import com.sushishop.service.ProductService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product with images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProductResponse> create(@RequestPart("product") @Valid CreateProductRequest request, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        log.info("POST /api/products - {}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request, images));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<Page<ProductListResponse>> getAll(@PageableDefault(size = 12, sort = "name") Pageable pageable, @RequestParam(required = false) String search, @RequestParam(required = false) Category category, @RequestParam(required = false) Boolean available) {
        log.info("GET /api/products - search: {}, category: {}, page: {}", search, category, pageable.getPageNumber());
        return ResponseEntity.ok(productService.getAll(pageable, search, category, available));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        log.info("GET /api/products - {}", id);
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular products", description = "Returns top 10 products sorted by rating and review count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of popular products")
    })
    public ResponseEntity<List<ProductListResponse>> getPopular() {
        return ResponseEntity.ok(productService.getPopular());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /api/products/{}", id);
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/products - {}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle product availability")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        log.info("PATCH /api/products/{}/toggle", id);
        productService.toggleAvailability(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add image to product")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> addImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        log.info("POST /api/products/{}/images", id);
        productService.addImage(id, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product image")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        log.info("DELETE /api/products/{}/images/{}", id, imageId);
        productService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }
}
