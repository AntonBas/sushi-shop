package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.product.ProductRepository;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PromotionValidator {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("End date cannot be in the past");
        }
    }

    public void validateTitleUnique(String title) {
        if (promotionRepository.findByTitle(title).isPresent()) {
            throw new BadRequestException("Promotion with title '" + title + "' already exists");
        }
    }

    public void validateProductsExist(List<Long> productIds) {
        Set<Long> uniqueIds = new HashSet<>(productIds);
        if (uniqueIds.size() != productIds.size()) {
            throw new BadRequestException("Duplicate product IDs are not allowed");
        }

        Set<Long> foundIds = productRepository.findAllById(productIds).stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (foundIds.size() != uniqueIds.size()) {
            var missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NotFoundException("Products not found: " + missingIds);
        }
    }

    public void validateProductsNotInOverlappingPromotions(Collection<Long> productIds, LocalDateTime startDate,
                                                          LocalDateTime endDate, Long excludePromotionId) {
        for (var promotion : promotionRepository.findActiveOverlapping(startDate, endDate)) {
            if (promotion.getId().equals(excludePromotionId)) {
                continue;
            }

            var conflictingProductNames = promotion.getProducts().stream()
                    .filter(product -> productIds.contains(product.getId()))
                    .map(Product::getName)
                    .toList();

            if (!conflictingProductNames.isEmpty()) {
                throw new BadRequestException(
                        "Products already in promotion '" + promotion.getTitle() + "' for an overlapping period: "
                                + String.join(", ", conflictingProductNames)
                );
            }
        }
    }
}
