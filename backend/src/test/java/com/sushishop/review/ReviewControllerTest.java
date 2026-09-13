package com.sushishop.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.review.dto.request.CreateReviewReplyRequest;
import com.sushishop.review.dto.request.CreateReviewRequest;
import com.sushishop.review.dto.response.ReviewReplyResponse;
import com.sushishop.review.dto.response.ReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReviewReplyService reviewReplyService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "anton@example.com")
    public void shouldCreateReview() throws Exception {
        var request = new CreateReviewRequest(1L, 5, "Very tasty!");
        var response = new ReviewResponse(1L, 1L, "Anton", 5, "Very tasty!", null, null, null);

        when(reviewService.create(any(), eq("anton@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @WithMockUser(username = "anton@example.com")
    public void shouldUpdateReview() throws Exception {
        var request = new CreateReviewRequest(1L, 4, "Updated!");
        var response = new ReviewResponse(1L, 1L, "Anton", 4, "Updated!", null, null, null);

        when(reviewService.update(eq(1L), any(), eq("anton@example.com"))).thenReturn(response);

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    public void shouldAddReply() throws Exception {
        var request = new CreateReviewReplyRequest("Thank you!");
        var response = new ReviewReplyResponse(1L, "Thank you!", "Admin", null);

        when(reviewReplyService.addReply(eq(1L), any(), eq("admin@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/reviews/1/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Thank you!"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    public void shouldUpdateReply() throws Exception {
        var request = new CreateReviewReplyRequest("Updated reply");
        var response = new ReviewReplyResponse(1L, "Updated reply", "Admin", null);

        when(reviewReplyService.updateReply(eq(1L), any(), eq("admin@example.com"))).thenReturn(response);

        mockMvc.perform(put("/api/reviews/replies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated reply"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    public void shouldDeleteReply() throws Exception {
        mockMvc.perform(delete("/api/reviews/replies/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "anton@example.com")
    public void shouldDeleteReview() throws Exception {
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "anton@example.com")
    public void shouldRejectNonAdminUserFromAddReply() throws Exception {
        var request = new CreateReviewReplyRequest("Thank you!");

        mockMvc.perform(post("/api/reviews/1/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}