package com.sushishop.product;

import com.sushishop.shared.enums.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
public class ProductRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void shouldSaveAndFindProduct() {
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
    public void shouldFindPopularProductsExcludingExtraCategory() {
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
    public void shouldFindBySlug() {
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
}