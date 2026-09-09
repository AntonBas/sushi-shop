package com.sushishop.payment;

import com.sushishop.order.Order;
import com.sushishop.order.OrderService;
import com.sushishop.shared.exception.core.BadRequestException;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @MockitoBean
    private WebhookIdempotencyService webhookIdempotencyService;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldCreateCheckoutSession() throws Exception {
        var order = Order.builder().id(1L).totalAmount(new BigDecimal("500.00")).build();
        when(orderService.getOrderById(1L)).thenReturn(order);
        var info = new StripeService.CheckoutSessionInfo("sess_123", "https://checkout.stripe.com/session_123");
        when(stripeService.createCheckoutSession(eq(1L), eq(50000L), eq("test@example.com")))
                .thenReturn(info);

        mockMvc.perform(post("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://checkout.stripe.com/session_123"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldExpireStripeSessionWhenPaymentCreationFails() throws Exception {
        var order = Order.builder().id(1L).totalAmount(new BigDecimal("500.00")).build();
        when(orderService.getOrderById(1L)).thenReturn(order);
        var info = new StripeService.CheckoutSessionInfo("sess_123", "https://checkout.stripe.com/session_123");
        when(stripeService.createCheckoutSession(eq(1L), eq(50000L), eq("test@example.com")))
                .thenReturn(info);
        doThrow(new BadRequestException("Order not found"))
                .when(paymentService).create(eq(1L), eq("sess_123"), any());

        mockMvc.perform(post("/api/payments/order/1"))
                .andExpect(status().isBadRequest());

        verify(stripeService).expireCheckoutSession("sess_123");
    }

    @Test
    void shouldHandleWebhook() throws Exception {
        when(stripeService.parseCheckoutCompletedEvent(anyString(), anyString()))
                .thenReturn(new StripeService.WebhookEvent("evt_123", "sess_123"));
        when(webhookIdempotencyService.markProcessed("evt_123")).thenReturn(true);

        mockMvc.perform(post("/api/payments/webhook")
                        .content("{}")
                        .header("Stripe-Signature", "sig_123"))
                .andExpect(status().isOk());

        verify(paymentService).confirmPayment("sess_123");
    }

    @Test
    void shouldSkipDuplicateWebhookEvent() throws Exception {
        when(stripeService.parseCheckoutCompletedEvent(anyString(), anyString()))
                .thenReturn(new StripeService.WebhookEvent("evt_123", "sess_123"));
        when(webhookIdempotencyService.markProcessed("evt_123")).thenReturn(false);

        mockMvc.perform(post("/api/payments/webhook")
                        .content("{}")
                        .header("Stripe-Signature", "sig_123"))
                .andExpect(status().isOk());

        verify(paymentService, never()).confirmPayment(anyString());
    }
}