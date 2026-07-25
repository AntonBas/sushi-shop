package com.sushishop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.domain.enums.DeliveryMethod;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.domain.enums.PaymentStatus;
import com.sushishop.dto.request.AddressRequest;
import com.sushishop.dto.request.CreateOrderRequest;
import com.sushishop.dto.request.OrderItemRequest;
import com.sushishop.dto.response.AddressResponse;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.dto.response.UserOrderResponse;
import com.sushishop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldCreateOrder() throws Exception {
        var request = new CreateOrderRequest("Anton", "+380961791111", DeliveryMethod.DELIVERY,
                new AddressRequest("Lviv", "Zelena", "204", "280", "code 123"),
                List.of(new OrderItemRequest(1L, 2)));

        var response = new OrderResponse(1L, "Anton", "+380961791111",
                new AddressResponse("Lviv", "Zelena", "204", "280", "code 123"),
                DeliveryMethod.DELIVERY, OrderStatus.NEW, PaymentStatus.PENDING, new BigDecimal("500.00"), null, List.of());

        when(orderService.create(any(CreateOrderRequest.class), eq("test@test.com"))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Anton"));
    }

    @Test
    void shouldReturn400WhenInvalidOrder() throws Exception {
        var request = new CreateOrderRequest("", "", DeliveryMethod.DELIVERY, null, List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldGetMyOrders() throws Exception {
        var response = new UserOrderResponse(1L, OrderStatus.NEW, PaymentStatus.PENDING, BigDecimal.ZERO, null, List.of());
        Page<UserOrderResponse> page = new PageImpl<>(List.of(response));

        when(orderService.getByUser(eq("test@test.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("NEW"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAllOrders() throws Exception {
        var response = new OrderResponse(1L, "Anton", "+380961791111", null, DeliveryMethod.PICKUP, OrderStatus.NEW, PaymentStatus.PENDING, BigDecimal.ZERO, null, List.of());
        Page<OrderResponse> page = new PageImpl<>(List.of(response));

        when(orderService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Anton"));
    }

    @Test
    void shouldGetById() throws Exception {
        var response = new OrderResponse(1L, "Anton", "+380961791111", null, DeliveryMethod.PICKUP, OrderStatus.NEW, PaymentStatus.PENDING, BigDecimal.ZERO, null, List.of());

        when(orderService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Anton"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldUpdateStatus() throws Exception {
        var response = new OrderResponse(1L, "Anton", "+380961791111", null, DeliveryMethod.PICKUP, OrderStatus.COOKING, PaymentStatus.PENDING, BigDecimal.ZERO, null, List.of());

        when(orderService.updateStatus(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "COOKING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COOKING"));
    }
}