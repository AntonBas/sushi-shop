package com.sushishop.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.order.dto.request.CreateOrderRequest;
import com.sushishop.order.dto.request.OrderItemRequest;
import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import com.sushishop.shared.address.AddressRequest;
import com.sushishop.shared.address.AddressResponse;
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

import static org.mockito.ArgumentMatchers.*;
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

    @MockitoBean
    private OrderCreationService orderCreationService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void shouldCreateOrder() throws Exception {
        var request = new CreateOrderRequest("Anton", "+380961791111", PaymentMethod.ON_DELIVERY, DeliveryMethod.DELIVERY,
                new AddressRequest("Lviv", "Zelena", "204", "280", "code 123"),
                List.of(new OrderItemRequest(1L, 2)));

        var response = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111",
                new AddressResponse("Lviv", "Zelena", "204", "280", "code 123"),
                DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW, new BigDecimal("500.00"), null, List.of());

        when(orderCreationService.create(any(CreateOrderRequest.class), eq("test@test.com"))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Anton"));
    }

    @Test
    public void shouldReturn400WhenInvalidOrder() throws Exception {
        var request = new CreateOrderRequest("", "", null, DeliveryMethod.DELIVERY, null, List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void shouldGetMyOrders() throws Exception {
        var response = new UserOrderResponse(1L, "test@test.com", OrderStatus.NEW, DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", BigDecimal.ZERO, null, List.of());
        Page<UserOrderResponse> page = new PageImpl<>(List.of(response));

        when(orderQueryService.getByUser(eq("test@test.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("NEW"))
                .andExpect(jsonPath("$.content[0].deliveryMethod").value("DELIVERY"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void shouldGetAllOrders() throws Exception {
        var response = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null, DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW, BigDecimal.ZERO, null, List.of());
        Page<OrderResponse> page = new PageImpl<>(List.of(response));

        when(orderQueryService.getAll(any(Pageable.class), isNull(), isNull(), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerName").value("Anton"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void shouldGetById() throws Exception {
        var response = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null, DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW, BigDecimal.ZERO, null, List.of());

        when(orderService.getById(1L, "test@test.com", false)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Anton"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void shouldUpdateStatus() throws Exception {
        var response = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null, DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.COOKING, BigDecimal.ZERO, null, List.of());

        when(orderService.updateStatus(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "COOKING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COOKING"));
    }
}