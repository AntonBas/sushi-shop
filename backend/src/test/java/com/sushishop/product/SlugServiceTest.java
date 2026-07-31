package com.sushishop.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SlugServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SlugService slugService;

    @Test
    void shouldGenerateSlug() {
        var result = slugService.generateSlug("California Roll");
        assertThat(result).isEqualTo("california-roll");
    }

    @Test
    void shouldGenerateSlugWithSpecialChars() {
        var result = slugService.generateSlug("Maki (8 pcs)");
        assertThat(result).isEqualTo("maki-8-pcs");
    }

    @Test
    void shouldGenerateUniqueSlug() {
        when(productRepository.findBySlug("california-roll")).thenReturn(Optional.empty());

        var result = slugService.generateUniqueSlug("California Roll");
        assertThat(result).isEqualTo("california-roll");
    }

    @Test
    void shouldAppendCounterWhenSlugExists() {
        when(productRepository.findBySlug("california-roll")).thenReturn(Optional.of(new Product()));
        when(productRepository.findBySlug("california-roll-1")).thenReturn(Optional.empty());

        var result = slugService.generateUniqueSlug("California Roll");
        assertThat(result).isEqualTo("california-roll-1");
    }
}