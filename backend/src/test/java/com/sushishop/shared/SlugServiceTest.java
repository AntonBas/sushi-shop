package com.sushishop.shared;

import com.sushishop.shared.service.SlugService;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class SlugServiceTest {

    private final SlugService slugService = new SlugService();

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
        var result = slugService.generateUniqueSlug("California Roll", slug -> false);
        assertThat(result).isEqualTo("california-roll");
    }

    @Test
    void shouldAppendCounterWhenSlugExists() {
        var result = slugService.generateUniqueSlug("California Roll", new Function<>() {
            private int count = 0;

            @Override
            public Boolean apply(String slug) {
                count++;
                return count < 3;
            }
        });
        assertThat(result).isEqualTo("california-roll-2");
    }

    @Test
    void shouldReturnEmptyForNullInput() {
        var result = slugService.generateSlug(null);
        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyForBlankInput() {
        var result = slugService.generateSlug("   ");
        assertThat(result).isEqualTo("");
    }
}