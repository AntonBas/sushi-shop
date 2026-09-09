package com.sushishop.review;

import com.sushishop.review.dto.request.CreateReviewReplyRequest;
import com.sushishop.review.dto.response.ReviewReplyResponse;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewReplyResponse addReply(Long reviewId, CreateReviewReplyRequest request, String userEmail) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var reply = ReviewReply.builder()
                .review(review)
                .user(user)
                .message(request.message())
                .build();

        var saved = reviewReplyRepository.save(reply);
        log.info("Reply added to review: {}", reviewId);
        return reviewMapper.toReplyResponse(saved);
    }

    @Transactional
    public ReviewReplyResponse updateReply(Long replyId, CreateReviewReplyRequest request, String userEmail) {
        var reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + replyId));

        if (!reply.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("You can only edit your own replies");
        }

        reply.setMessage(request.message());
        return reviewMapper.toReplyResponse(reviewReplyRepository.save(reply));
    }

    @Transactional
    public void deleteReply(Long replyId, String email) {
        var reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + replyId));

        if (!reply.getUser().getEmail().equals(email)) {
            throw new BadRequestException("You can only delete your own replies");
        }

        reviewReplyRepository.delete(reply);
    }
}