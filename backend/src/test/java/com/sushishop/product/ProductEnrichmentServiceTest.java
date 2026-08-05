package com.sushishop.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductEnrichmentServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductEnrichmentService productEnrichmentService;

    @Test
    void shouldReturnEmptyMapForEmptyList() {
        var result = productEnrichmentService.getAverageRatings(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnRatingsMap() {
        when(productRepository.findAverageRatingsByProductIds(List.of(1L, 2L)))
                .thenReturn(List.of(new Object[]{1L, 4.5}, new Object[]{2L, 3.0}));

        var result = productEnrichmentService.getAverageRatings(List.of(1L, 2L));

        assertThat(result).isEqualTo(Map.of(1L, 4.5, 2L, 3.0));
    }
}