package com.sushishop.product;

import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    public void shouldMapToResponse() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Maki");
        product.setDescription("Salmon roll");
        product.setPrice(new BigDecimal("250.00"));
        product.setCategory(Category.ROLL);
        product.setAvailable(true);
        product.setWeight(250);
        product.setPieces(8);
        product.setProductImages(new ArrayList<>());
        product.setPromotions(new HashSet<>());
        product.setReviews(new ArrayList<>());

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.images()).isEmpty();
    }

    @Test
    public void shouldMapToEntity() {
        var request = new CreateProductRequest(
                "Maki",
                "Salmon roll",
                new BigDecimal("250.00"),
                Category.ROLL,
                250,
                8
        );

        Product product = productMapper.toEntity(request);

        assertThat(product.getName()).isEqualTo("Maki");
        assertThat(product.getDescription()).isEqualTo("Salmon roll");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(product.getCategory()).isEqualTo(Category.ROLL);
        assertThat(product.getWeight()).isEqualTo(250);
        assertThat(product.getPieces()).isEqualTo(8);
    }

    @Test
    public void shouldUpdateEntity() {
        var request = new UpdateProductRequest(
                "Updated Maki",
                "Updated description",
                new BigDecimal("280.00"),
                Category.ROLL,
                300,
                10
        );

        Product product = Product.builder()
                .name("Maki")
                .description("Old description")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .weight(250)
                .pieces(8)
                .build();

        productMapper.updateEntity(request, product);

        assertThat(product.getName()).isEqualTo("Updated Maki");
        assertThat(product.getDescription()).isEqualTo("Updated description");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("280.00"));
        assertThat(product.getWeight()).isEqualTo(300);
        assertThat(product.getPieces()).isEqualTo(10);
    }

    @Test
    public void shouldMapToListResponse() {
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .weight(250)
                .pieces(8)
                .build();

        Double rating = 4.5;
        ProductListResponse response = productMapper.toListResponse(product, rating, null);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maki");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.category()).isEqualTo(Category.ROLL);
        assertThat(response.available()).isTrue();
        assertThat(response.weight()).isEqualTo(250);
        assertThat(response.pieces()).isEqualTo(8);
        assertThat(response.mainImage()).isNull();
        assertThat(response.averageRating()).isEqualTo(4.5);
        assertThat(response.discountedPrice()).isNull();
    }

    @Test
    public void shouldMapToListResponseWithDiscountedPrice() {
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .category(Category.ROLL)
                .available(true)
                .weight(250)
                .pieces(8)
                .build();

        ProductListResponse response = productMapper.toListResponse(product, null, new BigDecimal("200.00"));

        assertThat(response.discountedPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    public void shouldMapToListResponseWithNullRating() {
        Product product = Product.builder()
                .id(2L)
                .name("Uramaki")
                .price(new BigDecimal("300.00"))
                .category(Category.ROLL)
                .available(true)
                .weight(300)
                .pieces(6)
                .build();

        ProductListResponse response = productMapper.toListResponse(product, null, null);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.name()).isEqualTo("Uramaki");
        assertThat(response.averageRating()).isNull();
    }
}