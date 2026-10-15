package com.sushishop.payment;

import com.sushishop.shared.exception.core.ConflictException;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private StripeWebhookService stripeWebhookService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnCheckoutUrl() throws Exception {
        when(checkoutService.createCheckout(1L, "test@example.com"))
                .thenReturn("https://checkout.stripe.com/session_123");

        mockMvc.perform(post("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://checkout.stripe.com/session_123"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnConflictForNonPayableOrder() throws Exception {
        when(checkoutService.createCheckout(1L, "test@example.com"))
                .thenThrow(new ConflictException("Order is already paid"));

        mockMvc.perform(post("/api/payments/order/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldPassWebhookPayloadAndSignatureToService() throws Exception {
        mockMvc.perform(post("/api/payments/webhook")
                        .content("{}")
                        .header("Stripe-Signature", "sig_123"))
                .andExpect(status().isOk());

        verify(stripeWebhookService).handle("{}", "sig_123");
    }

    @Test
    void shouldReturnServerErrorWhenWebhookProcessingFails() throws Exception {
        doThrow(new RuntimeException("transient DB error")).when(stripeWebhookService).handle("{}", "sig_123");

        mockMvc.perform(post("/api/payments/webhook")
                        .content("{}")
                        .header("Stripe-Signature", "sig_123"))
                .andExpect(status().isInternalServerError());
    }
}
