package com.sushishop.service;

import com.sushishop.domain.Review;
import com.sushishop.dto.request.CreateReviewRequest;
import com.sushishop.dto.response.ReviewResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.ConflictException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.ReviewMapper;
import com.sushishop.repository.ProductRepository;
import com.sushishop.repository.ReviewRepository;
import com.sushishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    public ReviewResponse create(CreateReviewRequest request, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ConflictException("Review already exists!");
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