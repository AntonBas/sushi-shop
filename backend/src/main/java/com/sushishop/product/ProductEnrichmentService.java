package com.sushishop.product;

import com.sushishop.promotion.Promotion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductEnrichmentService {

    private final ProductRepository productRepository;

    public BigDecimal calculateDiscountedPrice(Product product) {
        var bestPromo = getBestPromo(product);
        if (bestPromo == null) return null;

        var discount = bestPromo.getDiscountPercent()
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return product.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDiscountPercent(Product product) {
        var bestPromo = getBestPromo(product);
        return bestPromo != null ? bestPromo.getDiscountPercent() : null;
    }

    public String getPromotionTitle(Product product) {
        var bestPromo = getBestPromo(product);
        return bestPromo != null ? bestPromo.getTitle() : null;
    }

    public Promotion getBestPromo(Product product) {
        if (product.getPromotions() == null || product.getPromotions().isEmpty()) return null;

        return product.getPromotions().stream()
                .filter(Promotion::isCurrentlyActive)
                .max(Comparator.comparing(Promotion::getDiscountPercent))
                .orElse(null);
    }

    public Map<Long, Double> getAverageRatings(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return Map.of();

        return productRepository.findAverageRatingsByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Double) row[1]
                ));
    }

    public void enrichProductsWithImagesAndPromotions(List<Product> products) {
        if (products == null || products.isEmpty()) return;

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        Map<Long, List<ProductImage>> imagesMap = productRepository
                .findImagesByProductIds(productIds)
                .stream()
                .collect(Collectors.groupingBy(img -> img.getProduct().getId()));

        Map<Long, Set<Promotion>> promotionsMap = productRepository
                .findWithPromotions(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Product::getPromotions));

        products.forEach(product -> {
            product.setProductImages(imagesMap.getOrDefault(product.getId(), Collections.emptyList()));

            Set<Promotion> promotions = promotionsMap.get(product.getId());
            if (promotions != null) {
                product.setPromotions(promotions);
            }
        });
    }
}