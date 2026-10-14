package com.sushishop.product;

import com.sushishop.promotion.Promotion;
import com.sushishop.promotion.PromotionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class ProductRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindProduct() {
        var product = productRepository.save(Product.builder()
                .name("Test Roll")
                .slug("test-roll")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .weight(250)
                .pieces(8)
                .build());

        var found = productRepository.findById(product.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Roll");
    }

    @Test
    void shouldFindPopularProductsExcludingExtraCategory() {
        productRepository.save(Product.builder()
                .name("Wasabi")
                .slug("wasabi-test")
                .price(new BigDecimal("10.00"))
                .category(Category.EXTRA)
                .weight(15)
                .pieces(1)
                .build());

        productRepository.save(Product.builder()
                .name("California")
                .slug("california-test")
                .price(new BigDecimal("169.00"))
                .category(Category.ROLL)
                .weight(230)
                .pieces(8)
                .build());

        var popular = productRepository.findPopular(Pageable.ofSize(10));

        assertThat(popular).isNotEmpty();
        assertThat(popular).noneMatch(p -> p.getCategory() == Category.EXTRA);
    }

    @Test
    void shouldFindBySlug() {
        productRepository.save(Product.builder()
                .name("Philadelphia")
                .slug("philadelphia-test")
                .price(new BigDecimal("290.00"))
                .category(Category.ROLL)
                .weight(280)
                .pieces(8)
                .build());

        var found = productRepository.findBySlug("philadelphia-test");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Philadelphia");
    }

    @Test
    @Transactional
    void shouldNotDuplicateImagesWhenProductHasSeveralPromotions() {
        var product = Product.builder()
                .name("Dragon")
                .slug("dragon-images-test")
                .price(new BigDecimal("270.00"))
                .category(Category.ROLL)
                .weight(290)
                .pieces(8)
                .build();
        for (int i = 0; i < 3; i++) {
            product.getProductImages().add(ProductImage.builder()
                    .url("https://example.com/dragon-" + i + ".jpg")
                    .sortOrder(i)
                    .product(product)
                    .build());
        }
        productRepository.save(product);
        for (int i = 0; i < 2; i++) {
            var promotion = Promotion.builder()
                    .slug("dragon-promo-" + i)
                    .title("Dragon promo " + i)
                    .discountPercent(new BigDecimal("10.00"))
                    .startDate(LocalDateTime.now().minusDays(10 - i * 5L))
                    .endDate(LocalDateTime.now().minusDays(6 - i * 5L))
                    .build();
            promotion.getProducts().add(product);
            promotionRepository.save(promotion);
        }
        entityManager.flush();
        entityManager.clear();

        var byId = productRepository.findById(product.getId()).orElseThrow();
        assertThat(byId.getPromotions()).hasSize(2);
        assertThat(byId.getProductImages()).hasSize(3);

        entityManager.clear();

        var bySlug = productRepository.findBySlug("dragon-images-test").orElseThrow();
        assertThat(bySlug.getProductImages()).hasSize(3);
    }
}