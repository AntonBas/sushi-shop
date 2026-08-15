package com.sushishop.review;

import com.sushishop.review.dto.request.CreateReviewReplyRequest;
import com.sushishop.review.dto.response.ReviewReplyResponse;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.User;
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
public class ReviewReplyServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewReplyService reviewReplyService;

    @Test
    public void shouldAddReply() {
        var user = User.builder().id(1L).email("admin@example.com").name("Admin").build();
        var review = Review.builder().id(1L).build();
        var request = new CreateReviewReplyRequest("Thank you!");
        var reply = ReviewReply.builder().id(1L).user(user).message("Thank you!").build();
        var expected = new ReviewReplyResponse(1L, "Thank you!", "Admin", null);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(reviewReplyRepository.save(any())).thenReturn(reply);
        when(reviewMapper.toReplyResponse(reply)).thenReturn(expected);

        var result = reviewReplyService.addReply(1L, request, "admin@example.com");

        assertThat(result.message()).isEqualTo("Thank you!");
        assertThat(result.authorName()).isEqualTo("Admin");
    }

    @Test
    public void shouldUpdateReply() {
        var user = User.builder().id(1L).email("admin@example.com").name("Admin").build();
        var reply = ReviewReply.builder().id(1L).user(user).message("Old message").build();
        var request = new CreateReviewReplyRequest("Updated message");
        var expected = new ReviewReplyResponse(1L, "Updated message", "Admin", null);

        when(reviewReplyRepository.findById(1L)).thenReturn(Optional.of(reply));
        when(reviewReplyRepository.save(any())).thenReturn(reply);
        when(reviewMapper.toReplyResponse(reply)).thenReturn(expected);

        var result = reviewReplyService.updateReply(1L, request, "admin@example.com");

        assertThat(result.message()).isEqualTo("Updated message");
    }

    @Test
    public void shouldDeleteReply() {
        var user = User.builder().id(1L).email("admin@example.com").build();
        var reply = ReviewReply.builder().id(1L).user(user).build();

        when(reviewReplyRepository.findById(1L)).thenReturn(Optional.of(reply));

        reviewReplyService.deleteReply(1L, "admin@example.com");

        verify(reviewReplyRepository).delete(reply);
    }

    @Test
    public void shouldThrowWhenUpdateReplyNotOwner() {
        var user = User.builder().id(1L).email("admin@example.com").build();
        var reply = ReviewReply.builder().id(1L).user(user).build();
        var request = new CreateReviewReplyRequest("Test");

        when(reviewReplyRepository.findById(1L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> reviewReplyService.updateReply(1L, request, "other@example.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldThrowWhenDeleteReplyNotOwner() {
        var user = User.builder().id(1L).email("admin@example.com").build();
        var reply = ReviewReply.builder().id(1L).user(user).build();

        when(reviewReplyRepository.findById(1L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> reviewReplyService.deleteReply(1L, "other@example.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldThrowWhenReviewNotFound() {
        var request = new CreateReviewReplyRequest("Test");

        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewReplyService.addReply(1L, request, "admin@example.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldThrowWhenUserNotFound() {
        var review = Review.builder().id(1L).build();
        var request = new CreateReviewReplyRequest("Test");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewReplyService.addReply(1L, request, "unknown@example.com"))
                .isInstanceOf(NotFoundException.class);
    }
}