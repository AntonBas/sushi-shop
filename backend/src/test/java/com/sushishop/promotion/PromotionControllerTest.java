package com.sushishop.promotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.request.UpdatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class PromotionControllerTest {

    private static final Long PROMOTION_ID = 1L;
    private static final String PROMOTION_SLUG = "weekend-sale";
    private static final String PROMOTION_TITLE = "Weekend Sale";
    private static final BigDecimal DISCOUNT_PERCENT = new BigDecimal("20.00");
    private static final boolean IS_CURRENTLY_ACTIVE = true;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PromotionService promotionService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private PromotionResponse createPromotionResponse(Long id, String slug, String title, BigDecimal discountPercent) {
        return new PromotionResponse(
                id,
                slug,
                title,
                "Test description",
                discountPercent,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                true,
                IS_CURRENTLY_ACTIVE,
                List.of()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldCreatePromotion() throws Exception {
        var request = new CreatePromotionRequest(PROMOTION_TITLE, "20% off", DISCOUNT_PERCENT, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7), List.of(1L));

        var response = createPromotionResponse(PROMOTION_ID, PROMOTION_SLUG, PROMOTION_TITLE, DISCOUNT_PERCENT);

        when(promotionService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(PROMOTION_TITLE))
                .andExpect(jsonPath("$.slug").value(PROMOTION_SLUG));

        verify(promotionService).create(any());
    }

    @Test
    public void shouldGetActivePromotions() throws Exception {
        var response = createPromotionResponse(PROMOTION_ID, PROMOTION_SLUG, PROMOTION_TITLE, DISCOUNT_PERCENT);

        when(promotionService.getActive()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(PROMOTION_TITLE));

        verify(promotionService).getActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldGetAllPromotions() throws Exception {
        var response = createPromotionResponse(PROMOTION_ID, PROMOTION_SLUG, PROMOTION_TITLE, DISCOUNT_PERCENT);

        when(promotionService.getAll(any(), any())).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(PROMOTION_TITLE));

        verify(promotionService).getAll(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldGetById() throws Exception {
        var response = createPromotionResponse(PROMOTION_ID, PROMOTION_SLUG, PROMOTION_TITLE, DISCOUNT_PERCENT);

        when(promotionService.getById(PROMOTION_ID)).thenReturn(response);

        mockMvc.perform(get("/api/promotions/{id}", PROMOTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(PROMOTION_TITLE));

        verify(promotionService).getById(PROMOTION_ID);
    }

    @Test
    public void shouldGetBySlug() throws Exception {
        var response = createPromotionResponse(PROMOTION_ID, PROMOTION_SLUG, PROMOTION_TITLE, DISCOUNT_PERCENT);

        when(promotionService.getBySlug(PROMOTION_SLUG)).thenReturn(response);

        mockMvc.perform(get("/api/promotions/slug/{slug}", PROMOTION_SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(PROMOTION_SLUG));

        verify(promotionService).getBySlug(PROMOTION_SLUG);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldUpdatePromotion() throws Exception {
        var request = new UpdatePromotionRequest("Updated Sale", "Updated description", new BigDecimal("30.00"), null, null, null, false);

        var response = createPromotionResponse(PROMOTION_ID, "updated-sale", "Updated Sale", new BigDecimal("30.00"));

        when(promotionService.update(eq(PROMOTION_ID), any())).thenReturn(response);

        mockMvc.perform(put("/api/promotions/{id}", PROMOTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Sale"));

        verify(promotionService).update(eq(PROMOTION_ID), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldDeletePromotion() throws Exception {
        mockMvc.perform(delete("/api/promotions/{id}", PROMOTION_ID))
                .andExpect(status().isNoContent());

        verify(promotionService).delete(PROMOTION_ID);
    }
}