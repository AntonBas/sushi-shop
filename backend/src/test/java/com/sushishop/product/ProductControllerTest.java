package com.sushishop.product;

import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import com.sushishop.shared.exception.core.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductQueryService productQueryService;

    @MockitoBean
    private ProductImageService productImageService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldGetAllProducts() throws Exception {
        var response = new ProductListResponse(1L, "maki", "Maki", new BigDecimal("250.00"), null, null, Category.ROLL, null, true, 250, 8);
        Page<ProductListResponse> page = new PageImpl<>(List.of(response));

        when(productQueryService.getAll(any(Pageable.class), isNull(), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Maki"));
    }

    @Test
    public void shouldGetById() throws Exception {
        var response = new ProductResponse(1L, "maki", "Maki", "Desc", new BigDecimal("250.00"), null, null, null, Category.ROLL, List.of(), 0, null, true, 250, 8);

        when(productService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maki"));
    }

    @Test
    public void shouldGetPopular() throws Exception {
        var response = new ProductListResponse(1L, "maki", "Maki", new BigDecimal("250.00"), null, 4.5, Category.ROLL, null, true, 250, 8);

        when(productQueryService.getPopular()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Maki"));
    }

    @Test
    public void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.getById(99L)).thenThrow(new NotFoundException("Product not found: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldRejectNonAdminUser() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldGetRelated() throws Exception {
        var response = new ProductListResponse(2L, "related", "Related", new BigDecimal("200.00"), null, null, Category.ROLL, null, true, null, null);

        when(productQueryService.getRelated(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/1/related"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Related"));
    }
}