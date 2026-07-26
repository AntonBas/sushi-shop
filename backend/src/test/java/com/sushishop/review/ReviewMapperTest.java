package com.sushishop.review;

import com.sushishop.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewMapperTest {

    @Autowired
    private ReviewMapper reviewMapper;

    @Test
    void shouldMapToResponse() {
        var user = User.builder().name("Anton").build();
        var review = Review.builder()
                .id(1L)
                .user(user)
                .rating(5)
                .comment("Very tasty!")
                .build();

        var response = reviewMapper.toResponse(review);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userName()).isEqualTo("Anton");
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Very tasty!");
        assertThat(response.replies()).isEmpty();
    }

    @Test
    void shouldMapToReplyResponse() {
        var user = User.builder().name("Admin").build();
        var reply = ReviewReply.builder()
                .id(1L)
                .user(user)
                .message("Thank you!")
                .build();

        var response = reviewMapper.toReplyResponse(reply);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.authorName()).isEqualTo("Admin");
        assertThat(response.message()).isEqualTo("Thank you!");
    }
}