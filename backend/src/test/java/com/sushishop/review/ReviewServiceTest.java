package com.sushishop.review;

import com.sushishop.product.Product;
import com.sushishop.user.User;
import com.sushishop.review.dto.request.CreateReviewReplyRequest;
import com.sushishop.review.dto.request.CreateReviewRequest;
import com.sushishop.review.dto.response.ReviewReplyResponse;
import com.sushishop.review.dto.response.ReviewResponse;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.product.ProductRepository;
import com.sushishop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void shouldCreateReview() {
        var request = new CreateReviewRequest(1L, 5, "Very tasty!");
        var user = User.builder().id(1L).email("anton@example.com").build();
        var product = Product.builder().id(1L).build();
        var expected = new ReviewResponse(1L, 1L, "Anton", 5, "Very tasty!", null, null, null);

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(reviewRepository.save(any())).thenReturn(new Review());
        when(reviewMapper.toResponse(any())).thenReturn(expected);

        var result = reviewService.create(request, "anton@example.com");

        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Very tasty!");
    }

    @Test
    void shouldUpdateReview() {
        var user = User.builder().id(1L).email("anton@example.com").build();
        var review = Review.builder().id(1L).user(user).rating(4).comment("Good").build();
        var request = new CreateReviewRequest(1L, 5, "Very tasty!");
        var expected = new ReviewResponse(1L, 1L, "Anton", 5, "Very tasty!", null, null, null);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(expected);

        var result = reviewService.update(1L, request, "anton@example.com");

        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Very tasty!");
    }

    @Test
    void shouldAddReply() {
        var user = User.builder().id(1L).email("admin@example.com").name("Admin").build();
        var review = Review.builder().id(1L).build();
        var request = new CreateReviewReplyRequest("Thank you!");
        var reply = ReviewReply.builder().id(1L).user(user).message("Thank you!").build();
        var expected = new ReviewReplyResponse(1L, "Thank you!", "Admin", null);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(reviewReplyRepository.save(any())).thenReturn(reply);
        when(reviewMapper.toReplyResponse(reply)).thenReturn(expected);

        var result = reviewService.addReply(1L, request, "admin@example.com");

        assertThat(result.message()).isEqualTo("Thank you!");
        assertThat(result.authorName()).isEqualTo("Admin");
    }

    @Test
    void shouldThrowWhenDuplicateReview() {
        var request = new CreateReviewRequest(1L, 5, "Great!");
        var user = User.builder().id(1L).email("anton@example.com").build();
        var product = Product.builder().id(1L).build();

        when(userRepository.findByEmail("anton@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(request, "anton@example.com"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        var request = new CreateReviewRequest(1L, 5, "Great!");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(request, "unknown@example.com"))
                .isInstanceOf(NotFoundException.class);
    }
}