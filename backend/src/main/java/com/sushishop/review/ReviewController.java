package com.sushishop.review;

import com.sushishop.review.dto.request.CreateReviewReplyRequest;
import com.sushishop.review.dto.request.CreateReviewRequest;
import com.sushishop.review.dto.response.ReviewReplyResponse;
import com.sushishop.review.dto.response.ReviewResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review endpoints")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;

    @PostMapping
    @Operation(summary = "Create a review")
    @ApiResponse(responseCode = "201", description = "Review created")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody CreateReviewRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/reviews - product: {}", request.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review")
    @ApiResponse(responseCode = "200", description = "Review updated")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody CreateReviewRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/reviews/{}", id);
        return ResponseEntity.ok(reviewService.update(id, request, userDetails.getUsername()));
    }

    @PutMapping("/replies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a reply")
    @ApiResponse(responseCode = "200", description = "Reply updated")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewReplyResponse> updateReply(@PathVariable Long id,
                                                           @Valid @RequestBody CreateReviewReplyRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/reviews/replies/{}", id);
        return ResponseEntity.ok(reviewReplyService.updateReply(id, request, userDetails.getUsername()));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product")
    public ResponseEntity<Page<ReviewResponse>> getByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/reviews/product/{}", productId);
        return ResponseEntity.ok(reviewService.getByProduct(productId, pageable));
    }

    @PostMapping("/{id}/replies")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add reply to review")
    @ApiResponse(responseCode = "201", description = "Reply created")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewReplyResponse> addReply(@PathVariable Long id,
                                                        @Valid @RequestBody CreateReviewReplyRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/reviews/{}/replies", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewReplyService.addReply(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    @ApiResponse(responseCode = "204", description = "Review deleted")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/reviews/{}", id);
        reviewService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/replies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a reply")
    @ApiResponse(responseCode = "204", description = "Reply deleted")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteReply(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/reviews/replies/{}", id);
        reviewReplyService.deleteReply(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}