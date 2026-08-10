package com.sushishop.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
@RequiredArgsConstructor
public class SlugService {

    public String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }

    public String generateUniqueSlug(String input, java.util.function.Function<String, Boolean> slugExists) {
        var baseSlug = generateSlug(input);
        var uniqueSlug = baseSlug;
        int counter = 1;

        while (slugExists.apply(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        return uniqueSlug;
    }
}