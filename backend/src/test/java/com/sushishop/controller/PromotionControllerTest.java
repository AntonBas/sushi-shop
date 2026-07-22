package com.sushishop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sushishop.dto.request.CreatePromotionRequest;
import com.sushishop.dto.response.PromotionResponse;
import com.sushishop.service.PromotionService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class PromotionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldCreatePromotion() throws Exception {
        var request = new CreatePromotionRequest("Weekend Sale", "20% off", new BigDecimal("20.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), List.of(1L));
        var response = new PromotionResponse(1L, "Weekend Sale", "20% off", new BigDecimal("20.00"),
                request.startDate(), request.endDate(), true, List.of());

        when(promotionService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Weekend Sale"));
    }

    @Test
    void shouldGetActivePromotions() throws Exception {
        var response = new PromotionResponse(1L, "Weekend Sale", null, new BigDecimal("20.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), true, List.of());

        when(promotionService.getActive()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Weekend Sale"));
    }

    @Test
    void shouldGetById() throws Exception {
        var response = new PromotionResponse(1L, "Weekend Sale", null, new BigDecimal("20.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), true, List.of());

        when(promotionService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Weekend Sale"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldDeletePromotion() throws Exception {
        mockMvc.perform(delete("/api/promotions/1"))
                .andExpect(status().isNoContent());
    }
}