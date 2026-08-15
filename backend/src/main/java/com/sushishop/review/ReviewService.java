package com.sushishop.review;

import com.sushishop.audit.Auditable;
import com.sushishop.product.ProductRepository;
import com.sushishop.review.dto.request.CreateReviewRequest;
import com.sushishop.review.dto.response.ReviewResponse;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    @Auditable(action = AuditAction.CREATE, entity = "Review")
    @Transactional
    @CacheEvict(value = {"products", "reviews"}, allEntries = true)
    public ReviewResponse create(CreateReviewRequest request, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ConflictException("You have already reviewed this product");
        }

        var review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        var saved = reviewRepository.save(review);
        log.info("Review created: {}", saved.getId());
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getByProduct(Long productId, Pageable pageable) {
        var idsPage = reviewRepository.findReviewIdsByProductId(productId, pageable);
        List<Long> ids = idsPage.getContent();

        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        var reviews = reviewRepository.findReviewsByIds(ids);
        var responses = reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, idsPage.getTotalElements());
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Review")
    @Transactional
    @CacheEvict(value = {"products", "reviews"}, allEntries = true)
    public ReviewResponse update(Long reviewId, CreateReviewRequest request, String email) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));

        if (!review.getUser().getEmail().equals(email)) {
            throw new BadRequestException("You can only edit your own reviews");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());
        var saved = reviewRepository.save(review);
        log.info("Review updated: {}", saved.getId());
        return reviewMapper.toResponse(saved);
    }

    @Auditable(action = AuditAction.DELETE, entity = "Review")
    @Transactional
    @CacheEvict(value = {"products", "reviews"}, allEntries = true)
    public void delete(Long reviewId, String email) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));

        if (!review.getUser().getEmail().equals(email)) {
            throw new BadRequestException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review deleted: {}", reviewId);
    }
}