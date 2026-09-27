package com.sushishop;

import com.sushishop.product.Category;
import com.sushishop.product.Product;
import com.sushishop.product.ProductRepository;
import com.sushishop.promotion.Promotion;
import com.sushishop.promotion.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class ApiIntegrationTest {

    private static final String CUSTOMER = "user@test.com";
    private static final String ADMIN = "admin@test.com";

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldStoreReviewCommentUpToApiLimit() throws Exception {
        var product = saveProduct();
        var comment = "a".repeat(250);

        mockMvc.perform(asCustomer(post("/api/reviews"))
                        .content("{\"productId\":" + product.getId() + ",\"rating\":5,\"comment\":\"" + comment + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value(comment));
    }

    @Test
    void shouldRejectSecondReviewForSameProductWithConflict() throws Exception {
        var product = saveProduct();
        var body = "{\"productId\":" + product.getId() + ",\"rating\":4,\"comment\":\"Tasty\"}";

        mockMvc.perform(asCustomer(post("/api/reviews")).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(asCustomer(post("/api/reviews")).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectDeletingOrderedProductAndKeepIt() throws Exception {
        var product = saveProduct();
        mockMvc.perform(asCustomer(post("/api/orders"))
                        .content("""
                                {"customerName":"Test User","phone":"+380991234567","paymentMethod":"ON_DELIVERY",
                                 "deliveryMethod":"PICKUP","items":[{"productId":%d,"quantity":2}]}
                                """.formatted(product.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(asAdmin(delete("/api/products/" + product.getId())))
                .andExpect(status().isConflict());

        assertThat(productRepository.findById(product.getId())).isPresent();
    }

    @Test
    void shouldAllowEditingPromotionThatAlreadyEnded() throws Exception {
        var promotion = promotionRepository.save(Promotion.builder()
                .title("Ended " + UUID.randomUUID())
                .slug("ended-" + UUID.randomUUID())
                .discountPercent(new BigDecimal("10"))
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(1))
                .active(true)
                .products(new HashSet<>(Set.of(saveProduct())))
                .build());

        mockMvc.perform(asAdmin(put("/api/promotions/" + promotion.getId()))
                        .content("{\"description\":\"Archived\",\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private Product saveProduct() {
        var suffix = UUID.randomUUID().toString().substring(0, 8);
        return productRepository.save(Product.builder()
                .name("Roll " + suffix)
                .slug("roll-" + suffix)
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .weight(250)
                .available(true)
                .build());
    }

    private MockHttpServletRequestBuilder asCustomer(MockHttpServletRequestBuilder request) {
        return request.with(user(CUSTOMER).roles("CUSTOMER"))
                .header("X-Requested-With", "XMLHttpRequest")
                .contentType(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        return request.with(user(ADMIN).roles("ADMIN"))
                .header("X-Requested-With", "XMLHttpRequest")
                .contentType(MediaType.APPLICATION_JSON);
    }
}
