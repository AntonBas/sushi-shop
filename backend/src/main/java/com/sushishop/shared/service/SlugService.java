package com.sushishop.shared.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.function.Predicate;

@Service
public class SlugService {

    private static final int MAX_SLUG_LENGTH = 100;

    public String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();

        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH);
        }

        return slug;
    }

    public String generateUniqueSlug(String input, Predicate<String> slugExists) {
        var baseSlug = generateSlug(input);
        var uniqueSlug = baseSlug;
        int counter = 1;

        while (slugExists.test(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        return uniqueSlug;
    }
}