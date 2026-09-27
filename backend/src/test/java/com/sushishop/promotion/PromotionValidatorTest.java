package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.product.ProductRepository;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionValidatorTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2030, 1, 31, 0, 0);

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PromotionValidator validator;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void validateDates_shouldThrowWhenStartAfterEnd() {
        var start = now.plusDays(2);
        var end = now.plusDays(1);

        assertThatThrownBy(() -> validator.validateDates(start, end)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateDates_shouldThrowWhenEndInPast() {
        var start = now.minusDays(2);
        var end = now.minusDays(1);

        assertThatThrownBy(() -> validator.validateDates(start, end)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateDates_shouldPassWhenDatesValid() {
        var start = now.plusDays(1);
        var end = now.plusDays(2);

        assertThatCode(() -> validator.validateDates(start, end)).doesNotThrowAnyException();
    }

    @Test
    void validateTitleUnique_shouldThrowWhenTitleExists() {
        var title = "Existing Promotion";
        when(promotionRepository.findByTitle(title))
                .thenReturn(Optional.of(new Promotion()));

        assertThatThrownBy(() -> validator.validateTitleUnique(title)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateTitleUnique_shouldPassWhenTitleFree() {
        var title = "New Promotion";
        when(promotionRepository.findByTitle(title))
                .thenReturn(Optional.empty());

        assertThatCode(() -> validator.validateTitleUnique(title)).doesNotThrowAnyException();
    }

    @Test
    void validateProductsExist_shouldThrowWhenDuplicateIds() {
        var productIds = List.of(1L, 1L, 2L);

        assertThatThrownBy(() -> validator.validateProductsExist(productIds)).isInstanceOf(BadRequestException.class);
        verify(productRepository, never()).findAllById(any());
    }

    @Test
    void validateProductsExist_shouldThrowWhenSomeMissing() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        when(productRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(product1, product2));

        assertThatThrownBy(() -> validator.validateProductsExist(List.of(1L, 2L, 3L))).isInstanceOf(NotFoundException.class);
    }

    @Test
    void validateProductsExist_shouldPassWhenAllExist() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        when(productRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(product1, product2));

        assertThatCode(() -> validator.validateProductsExist(List.of(1L, 2L))).doesNotThrowAnyException();
    }

    @Test
    void validateProductsNotInOverlappingPromotions_shouldThrowWhenConflict() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        var existingPromotion = Promotion.builder()
                .id(10L)
                .title("Existing Promotion")
                .products(new HashSet<>(List.of(product1, product2)))
                .build();

        when(promotionRepository.findActiveOverlapping(START, END))
                .thenReturn(List.of(existingPromotion));

        assertThatThrownBy(() -> validator.validateProductsNotInOverlappingPromotions(List.of(1L), START, END, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateProductsNotInOverlappingPromotions_shouldSkipExcludedPromotion() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();

        var existingPromotion = Promotion.builder()
                .id(10L)
                .title("Existing Promotion")
                .products(new HashSet<>(List.of(product1)))
                .build();

        when(promotionRepository.findActiveOverlapping(START, END))
                .thenReturn(List.of(existingPromotion));

        assertThatCode(() -> validator.validateProductsNotInOverlappingPromotions(List.of(1L), START, END, 10L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateProductsNotInOverlappingPromotions_shouldPassWhenNoConflict() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        var existingPromotion = Promotion.builder()
                .id(10L)
                .title("Existing Promotion")
                .products(new HashSet<>(List.of(product1)))
                .build();

        when(promotionRepository.findActiveOverlapping(START, END))
                .thenReturn(List.of(existingPromotion));

        assertThatCode(() -> validator.validateProductsNotInOverlappingPromotions(List.of(2L), START, END, null))
                .doesNotThrowAnyException();
    }
}