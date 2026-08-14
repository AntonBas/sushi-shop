package com.sushishop.review;

import com.sushishop.audit.Auditable;
import com.sushishop.product.ProductRepository;
import com.sushishop.review.dto.request.CreateReviewReplyRequest;
import com.sushishop.review.dto.request.CreateReviewRequest;
import com.sushishop.review.dto.response.ReviewReplyResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewReplyRepository reviewReplyRepository;

    @Auditable(action = AuditAction.CREATE, entity = "Review")
    @Transactional
    @CacheEvict(value = {"products", "reviews"}, key = "#request.productId()")
    public ReviewResponse create(CreateReviewRequest request, String email) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        var product = productRepository.findById(request.productId()).orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ConflictException("You have already reviewed this product");
        }

        var review = Review.builder().user(user).product(product).rating(request.rating()).comment(request.comment()).build();

        var saved = reviewRepository.save(review);
        log.info("Review created: {}", saved.getId());
        return reviewMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "reviews", key = "#reviewId")
    public ReviewReplyResponse addReply(Long reviewId, CreateReviewReplyRequest request, String userEmail) {
        var review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));
        var user = userRepository.findByEmail(userEmail).orElseThrow(() -> new NotFoundException("User not found"));

        var reply = ReviewReply.builder().review(review).user(user).message(request.message()).build();

        var saved = reviewReplyRepository.save(reply);
        log.info("Reply added to review: {}", reviewId);
        return reviewMapper.toReplyResponse(saved);
    }

    public Page<ReviewResponse> getByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(reviewMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = {"products", "reviews"}, key = "#review.product.id")
    public ReviewResponse update(Long reviewId, CreateReviewRequest request, String email) {
        var review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));
        if (!review.getUser().getEmail().equals(email)) {
            throw new BadRequestException("You can only edit your own reviews");
        }
        review.setRating(request.rating());
        review.setComment(request.comment());
        var saved = reviewRepository.save(review);
        log.info("Review updated: {}", saved.getId());
        return reviewMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "reviews", key = "#replyId")
    public ReviewReplyResponse updateReply(Long replyId, CreateReviewReplyRequest request, String userEmail) {
        var reply = reviewReplyRepository.findById(replyId).orElseThrow(() -> new NotFoundException("Review not found: " + replyId));
        if (!reply.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("You can only edit your own reviews");
        }
        reply.setMessage(request.message());
        return reviewMapper.toReplyResponse(reviewReplyRepository.save(reply));
    }

    @Auditable(action = AuditAction.DELETE, entity = "Review")
    @Transactional
    @CacheEvict(value = {"products", "reviews"}, key = "#review.product.id")
    public void delete(Long reviewId, String email) {
        var review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));
        if (!review.getUser().getEmail().equals(email)) {
            throw new BadRequestException("You can only delete your own reviews");
        }
        reviewRepository.delete(review);
        log.info("Review deleted: {}", reviewId);
    }

    @Transactional
    @CacheEvict(value = "reviews", key = "#replyId")
    public void deleteReply(Long replyId, String email) {
        var reply = reviewReplyRepository.findById(replyId).orElseThrow(() -> new NotFoundException("Review not found: " + replyId));
        if (!reply.getUser().getEmail().equals(email)) {
            throw new BadRequestException("You can only delete your own reviews");
        }
        reviewReplyRepository.delete(reply);
    }
}