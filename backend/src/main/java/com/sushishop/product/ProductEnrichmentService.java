package com.sushishop.product;

import com.sushishop.promotion.Promotion;
import com.sushishop.review.Review;
import org.mapstruct.Named;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class ProductEnrichmentService {

    @Named("discountedPrice")
    public BigDecimal calculateDiscountedPrice(Product product) {
        var bestPromo = getBestPromo(product);
        if (bestPromo == null) return null;
        var discount = bestPromo.getDiscountPercent().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return product.getPrice().multiply(BigDecimal.ONE.subtract(discount));
    }

    @Named("discountPercent")
    public BigDecimal getDiscountPercent(Product product) {
        var bestPromo = getBestPromo(product);
        return bestPromo != null ? bestPromo.getDiscountPercent() : null;
    }

    @Named("promotionTitle")
    public String getPromotionTitle(Product product) {
        var bestPromo = getBestPromo(product);
        return bestPromo != null ? bestPromo.getTitle() : null;
    }

    @Named("averageRating")
    public Double getAverageRating(Product product) {
        if (product.getReviews().isEmpty()) return null;
        return product.getReviews().stream()
                .mapToInt(Review::getRating)
                .average().orElse(0.0);
    }

    public Promotion getBestPromo(Product product) {
        return product.getPromotions().stream()
                .filter(p -> p.isActive() && p.getEndDate().isAfter(LocalDateTime.now()))
                .max(Comparator.comparing(Promotion::getDiscountPercent))
                .orElse(null);
    }
}
