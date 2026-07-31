package com.sushishop.product;

import com.sushishop.shared.exception.core.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
@RequiredArgsConstructor
public class SlugService {
    private final ProductRepository productRepository;

    public String generateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BadRequestException("Product name is required for slug generation");
        }

        return Normalizer.normalize(slug, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "-").toLowerCase();
    }

    public String generateUniqueSlug(String slug) {
        var baseSlug = generateSlug(slug);
        var uniqueSlug = baseSlug;
        int counter = 1;

        while (productRepository.findBySlug(uniqueSlug).isPresent()) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        return uniqueSlug;
    }
}
