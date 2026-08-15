package com.sushishop.product;

import com.sushishop.promotion.Promotion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductEnrichmentServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductEnrichmentService productEnrichmentService;

    @Test
    public void shouldReturnEmptyMapForEmptyList() {
        var result = productEnrichmentService.getAverageRatings(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnRatingsMap() {
        when(productRepository.findAverageRatingsByProductIds(List.of(1L, 2L)))
                .thenReturn(List.of(new Object[]{1L, 4.5}, new Object[]{2L, 3.0}));

        var result = productEnrichmentService.getAverageRatings(List.of(1L, 2L));

        assertThat(result).isEqualTo(Map.of(1L, 4.5, 2L, 3.0));
    }

    @Test
    public void shouldEnrichProductsWithImagesAndPromotions() {
        Product product1 = Product.builder().id(1L).name("Product 1").build();
        Product product2 = Product.builder().id(2L).name("Product 2").build();
        List<Product> products = Arrays.asList(product1, product2);

        ProductImage image1 = ProductImage.builder().id(1L).url("img1.jpg").sortOrder(0).product(product1).build();
        ProductImage image2 = ProductImage.builder().id(2L).url("img2.jpg").sortOrder(1).product(product1).build();
        ProductImage image3 = ProductImage.builder().id(3L).url("img3.jpg").sortOrder(0).product(product2).build();

        Promotion promotion = Promotion.builder()
                .id(1L)
                .title("Sale")
                .discountPercent(new BigDecimal("20"))
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        Product enriched1 = Product.builder().id(1L).promotions(Set.of(promotion)).build();
        Product enriched2 = Product.builder().id(2L).promotions(Set.of()).build();

        when(productRepository.findImagesByProductIds(List.of(1L, 2L)))
                .thenReturn(List.of(image1, image2, image3));
        when(productRepository.findWithPromotions(List.of(1L, 2L)))
                .thenReturn(List.of(enriched1, enriched2));

        productEnrichmentService.enrichProductsWithImagesAndPromotions(products);

        assertThat(product1.getProductImages()).hasSize(2);
        assertThat(product2.getProductImages()).hasSize(1);
        assertThat(product1.getPromotions()).hasSize(1);
        assertThat(product2.getPromotions()).isEmpty();
    }

    @Test
    public void shouldEnrichEmptyList() {
        productEnrichmentService.enrichProductsWithImagesAndPromotions(List.of());
    }

    @Test
    public void shouldCalculateDiscountedPrice() {
        var promotion = Promotion.builder()
                .id(1L)
                .title("Sale")
                .discountPercent(new BigDecimal("20.00"))
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .promotions(Set.of(promotion))
                .build();

        var result = productEnrichmentService.calculateDiscountedPrice(product);

        assertThat(result).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    public void shouldReturnNullDiscountedPriceWhenNoActivePromo() {
        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .promotions(Set.of())
                .build();

        var result = productEnrichmentService.calculateDiscountedPrice(product);

        assertThat(result).isNull();
    }

    @Test
    public void shouldGetDiscountPercent() {
        var promotion = Promotion.builder()
                .id(1L)
                .title("Sale")
                .discountPercent(new BigDecimal("20.00"))
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .promotions(Set.of(promotion))
                .build();

        var result = productEnrichmentService.getDiscountPercent(product);

        assertThat(result).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    public void shouldGetPromotionTitle() {
        var promotion = Promotion.builder()
                .id(1L)
                .title("Weekend Sale")
                .discountPercent(new BigDecimal("20.00"))
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .promotions(Set.of(promotion))
                .build();

        var result = productEnrichmentService.getPromotionTitle(product);

        assertThat(result).isEqualTo("Weekend Sale");
    }

    @Test
    public void shouldGetBestPromoWithHighestDiscount() {
        var promo20 = Promotion.builder()
                .id(1L)
                .title("20% Sale")
                .discountPercent(new BigDecimal("20.00"))
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        var promo30 = Promotion.builder()
                .id(2L)
                .title("30% Sale")
                .discountPercent(new BigDecimal("30.00"))
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        var product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .promotions(Set.of(promo20, promo30))
                .build();

        var result = productEnrichmentService.getBestPromo(product);

        assertThat(result.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("30.00"));
    }
}