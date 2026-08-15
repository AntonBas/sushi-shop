package com.sushishop.shared;

import com.sushishop.shared.service.SlugService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class SlugServiceTest {

    private final SlugService slugService = new SlugService();

    @Test
    public void shouldGenerateSlug() {
        var result = slugService.generateSlug("California Roll");
        assertThat(result).isEqualTo("california-roll");
    }

    @Test
    public void shouldGenerateSlugWithSpecialChars() {
        var result = slugService.generateSlug("Maki (8 pcs)");
        assertThat(result).isEqualTo("maki-8-pcs");
    }

    @Test
    public void shouldGenerateUniqueSlug() {
        var result = slugService.generateUniqueSlug("California Roll", slug -> false);
        assertThat(result).isEqualTo("california-roll");
    }

    @Test
    public void shouldAppendCounterWhenSlugExists() {
        var counter = new AtomicInteger(0);
        Predicate<String> slugExists = slug -> {
            counter.incrementAndGet();
            return counter.get() < 3;
        };

        var result = slugService.generateUniqueSlug("California Roll", slugExists);

        assertThat(result).isEqualTo("california-roll-2");
    }

    @Test
    public void shouldReturnEmptyForNullInput() {
        var result = slugService.generateSlug(null);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void shouldReturnEmptyForBlankInput() {
        var result = slugService.generateSlug("   ");
        assertThat(result).isEqualTo("");
    }
}