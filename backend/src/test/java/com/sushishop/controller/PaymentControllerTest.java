package com.sushishop.controller;

import com.sushishop.service.PaymentService;
import com.sushishop.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser
    void shouldCreateCheckoutSession() throws Exception {
        when(stripeService.createCheckoutSession(eq(1L), eq(50000L), eq("test@example.com")))
                .thenReturn("https://checkout.stripe.com/session_123");

        mockMvc.perform(post("/api/payments/order/1")
                        .param("amountInCents", "50000")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://checkout.stripe.com/session_123"));
    }

    @Test
    void shouldHandleWebhook() throws Exception {
        when(stripeService.getSessionIdFromWebhook(anyString(), anyString()))
                .thenReturn("sess_123");

        mockMvc.perform(post("/api/payments/webhook")
                        .content("{}")
                        .header("Stripe-Signature", "sig_123"))
                .andExpect(status().isOk());
    }
}