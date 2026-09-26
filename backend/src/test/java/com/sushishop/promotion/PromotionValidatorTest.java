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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromotionValidatorTest {

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
    public void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    public void validateDates_shouldThrowWhenStartAfterEnd() {
        var start = now.plusDays(2);
        var end = now.plusDays(1);

        assertThrows(BadRequestException.class,
                () -> validator.validateDates(start, end));
    }

    @Test
    public void validateDates_shouldThrowWhenEndInPast() {
        var start = now.minusDays(2);
        var end = now.minusDays(1);

        assertThrows(BadRequestException.class,
                () -> validator.validateDates(start, end));
    }

    @Test
    public void validateDates_shouldPassWhenDatesValid() {
        var start = now.plusDays(1);
        var end = now.plusDays(2);

        assertDoesNotThrow(() -> validator.validateDates(start, end));
    }

    @Test
    public void validateTitleUnique_shouldThrowWhenTitleExists() {
        var title = "Existing Promotion";
        when(promotionRepository.findByTitle(title))
                .thenReturn(Optional.of(new Promotion()));

        assertThrows(BadRequestException.class,
                () -> validator.validateTitleUnique(title));
    }

    @Test
    public void validateTitleUnique_shouldPassWhenTitleFree() {
        var title = "New Promotion";
        when(promotionRepository.findByTitle(title))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validateTitleUnique(title));
    }

    @Test
    public void validateProductsExist_shouldThrowWhenDuplicateIds() {
        var productIds = List.of(1L, 1L, 2L);

        assertThrows(BadRequestException.class,
                () -> validator.validateProductsExist(productIds));
        verify(productRepository, never()).findAllById(any());
    }

    @Test
    public void validateProductsExist_shouldThrowWhenSomeMissing() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        when(productRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(product1, product2));

        assertThrows(NotFoundException.class,
                () -> validator.validateProductsExist(List.of(1L, 2L, 3L)));
    }

    @Test
    public void validateProductsExist_shouldPassWhenAllExist() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        when(productRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(product1, product2));

        assertDoesNotThrow(() -> validator.validateProductsExist(List.of(1L, 2L)));
    }

    @Test
    public void validateProductsNotInOverlappingPromotions_shouldThrowWhenConflict() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        var existingPromotion = Promotion.builder()
                .id(10L)
                .title("Existing Promotion")
                .products(new HashSet<>(List.of(product1, product2)))
                .build();

        when(promotionRepository.findActiveOverlapping(START, END))
                .thenReturn(List.of(existingPromotion));

        assertThrows(BadRequestException.class,
                () -> validator.validateProductsNotInOverlappingPromotions(List.of(1L), START, END, null));
    }

    @Test
    public void validateProductsNotInOverlappingPromotions_shouldSkipExcludedPromotion() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();

        var existingPromotion = Promotion.builder()
                .id(10L)
                .title("Existing Promotion")
                .products(new HashSet<>(List.of(product1)))
                .build();

        when(promotionRepository.findActiveOverlapping(START, END))
                .thenReturn(List.of(existingPromotion));

        assertDoesNotThrow(() ->
                validator.validateProductsNotInOverlappingPromotions(List.of(1L), START, END, 10L));
    }

    @Test
    public void validateProductsNotInOverlappingPromotions_shouldPassWhenNoConflict() {
        var product1 = Product.builder().id(1L).name("Roll 1").build();
        var product2 = Product.builder().id(2L).name("Roll 2").build();

        var existingPromotion = Promotion.builder()
                .id(10L)
                .title("Existing Promotion")
                .products(new HashSet<>(List.of(product1)))
                .build();

        when(promotionRepository.findActiveOverlapping(START, END))
                .thenReturn(List.of(existingPromotion));

        assertDoesNotThrow(() ->
                validator.validateProductsNotInOverlappingPromotions(List.of(2L), START, END, null));
    }
}