package com.sushishop.product;

import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.shared.exception.core.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private static final int POPULAR_PRODUCTS_LIMIT = 10;
    private static final int RELATED_PRODUCTS_LIMIT = 4;

    private final ProductRepository productRepository;
    private final ProductEnrichmentService enrichmentService;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getAll(Pageable pageable, String search, Category category, Boolean available) {
        var spec = Specification.where(ProductSpecification.hasSearch(search))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.isAvailable(available));

        var ratingOrder = pageable.getSort().getOrderFor("rating");
        if (ratingOrder != null) {
            return getAllSortedByRating(spec, pageable, ratingOrder.getDirection());
        }

        var page = productRepository.findAll(spec, pageable);
        List<Product> products = page.getContent();

        if (products.isEmpty()) {
            return Page.empty(pageable);
        }

        var enriched = enrichProducts(products);
        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    private Page<ProductListResponse> getAllSortedByRating(Specification<Product> spec, Pageable pageable, Sort.Direction direction) {
        List<Product> matching = productRepository.findAll(spec);
        if (matching.isEmpty()) {
            return Page.empty(pageable);
        }

        var productIds = matching.stream().map(Product::getId).toList();
        var ratings = enrichmentService.getAverageRatings(productIds);

        Comparator<Product> byRating = Comparator.comparingDouble(p -> ratings.getOrDefault(p.getId(), 0.0));
        var sorted = matching.stream()
                .sorted(direction == Sort.Direction.DESC ? byRating.reversed() : byRating)
                .toList();

        int start = Math.min((int) pageable.getOffset(), sorted.size());
        int end = Math.min(start + pageable.getPageSize(), sorted.size());

        var enriched = enrichProducts(sorted.subList(start, end));
        return new PageImpl<>(enriched, pageable, sorted.size());
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getPopular() {
        List<Product> products = productRepository.findPopular(Pageable.ofSize(POPULAR_PRODUCTS_LIMIT));
        return enrichProducts(products);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getRelated(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        List<Product> relatedProducts = productRepository.findRelated(
                product.getCategory(), id, Pageable.ofSize(RELATED_PRODUCTS_LIMIT));

        return enrichProducts(relatedProducts);
    }

    private List<ProductListResponse> enrichProducts(List<Product> products) {
        if (products.isEmpty()) return List.of();

        enrichmentService.enrichProductsWithImagesAndPromotions(products);

        var productIds = products.stream().map(Product::getId).toList();
        var ratings = enrichmentService.getAverageRatings(productIds);

        return mapToResponseList(products, ratings);
    }

    private List<ProductListResponse> mapToResponseList(List<Product> products, Map<Long, Double> ratings) {
        return products.stream()
                .map(product -> {
                    var discount = enrichmentService.calculateDiscountedPrice(product);
                    return productMapper.toListResponse(product, ratings.get(product.getId()), discount);
                })
                .toList();
    }
}