package com.sushishop.promotion;

import com.sushishop.product.Category;
import com.sushishop.product.Product;
import com.sushishop.product.ProductEnrichmentService;
import com.sushishop.product.ProductMapper;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.promotion.dto.response.PromotionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromotionResponseAssemblerTest {

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductEnrichmentService productEnrichmentService;

    @InjectMocks
    private PromotionResponseAssembler assembler;

    private Promotion promotion;
    private Product product1;
    private Product product2;
    private PromotionResponse baseResponse;

    @BeforeEach
    public void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("Roll 1")
                .price(new BigDecimal("10.00"))
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Roll 2")
                .price(new BigDecimal("20.00"))
                .build();

        promotion = Promotion.builder()
                .id(1L)
                .slug("test-promotion")
                .title("Test Promotion")
                .description("Test description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .products(Set.of(product1, product2))
                .build();

        baseResponse = new PromotionResponse(
                1L,
                "test-promotion",
                "Test Promotion",
                "Test description",
                new BigDecimal("20.00"),
                promotion.getStartDate(),
                promotion.getEndDate(),
                true,
                true,
                null
        );
    }

    private ProductListResponse createProductResponse(Long id, String name, BigDecimal price, BigDecimal discountedPrice, Double rating) {
        return new ProductListResponse(
                id,
                "slug-" + id,
                name,
                price,
                discountedPrice,
                rating,
                Category.ROLL,
                null,
                true,
                null,
                null
        );
    }

    @Test
    public void toResponse_shouldApplyDiscountWhenPromotionActive() {
        when(promotionMapper.toResponse(promotion)).thenReturn(baseResponse);
        when(productEnrichmentService.getAverageRatings(anyList()))
                .thenReturn(Map.of(1L, 4.5, 2L, 4.0));

        var productResponse1 = createProductResponse(1L, "Roll 1", new BigDecimal("10.00"), new BigDecimal("8.00"), 4.5);
        var productResponse2 = createProductResponse(2L, "Roll 2", new BigDecimal("20.00"), new BigDecimal("16.00"), 4.0);

        when(productMapper.toListResponse(product1, 4.5, new BigDecimal("8.00")))
                .thenReturn(productResponse1);
        when(productMapper.toListResponse(product2, 4.0, new BigDecimal("16.00")))
                .thenReturn(productResponse2);

        var result = assembler.toResponse(promotion);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(2, result.products().size());
        verify(productEnrichmentService).getAverageRatings(anyList());
    }

    @Test
    public void toResponse_shouldNotApplyDiscountWhenPromotionInactive() {
        promotion.setActive(false);
        when(promotionMapper.toResponse(promotion)).thenReturn(baseResponse);
        when(productEnrichmentService.getAverageRatings(anyList()))
                .thenReturn(Map.of(1L, 4.5, 2L, 4.0));

        var productResponse1 = createProductResponse(1L, "Roll 1", new BigDecimal("10.00"), null, 4.5);
        var productResponse2 = createProductResponse(2L, "Roll 2", new BigDecimal("20.00"), null, 4.0);

        when(productMapper.toListResponse(product1, 4.5, null))
                .thenReturn(productResponse1);
        when(productMapper.toListResponse(product2, 4.0, null))
                .thenReturn(productResponse2);

        var result = assembler.toResponse(promotion);

        assertNotNull(result);
        verify(productMapper).toListResponse(product1, 4.5, null);
        verify(productMapper).toListResponse(product2, 4.0, null);
    }

    @Test
    public void toResponse_shouldNotApplyDiscountWhenNotInDateRange() {
        promotion.setStartDate(LocalDateTime.now().plusDays(1));
        promotion.setEndDate(LocalDateTime.now().plusDays(2));

        when(promotionMapper.toResponse(promotion)).thenReturn(baseResponse);
        when(productEnrichmentService.getAverageRatings(anyList()))
                .thenReturn(Map.of(1L, 4.5, 2L, 4.0));

        var productResponse1 = createProductResponse(1L, "Roll 1", new BigDecimal("10.00"), null, 4.5);
        var productResponse2 = createProductResponse(2L, "Roll 2", new BigDecimal("20.00"), null, 4.0);

        when(productMapper.toListResponse(product1, 4.5, null))
                .thenReturn(productResponse1);
        when(productMapper.toListResponse(product2, 4.0, null))
                .thenReturn(productResponse2);

        var result = assembler.toResponse(promotion);

        assertNotNull(result);
        verify(productMapper).toListResponse(product1, 4.5, null);
    }
}