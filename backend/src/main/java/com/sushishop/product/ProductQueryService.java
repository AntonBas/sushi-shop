package com.sushishop.product;

import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.shared.enums.Category;
import com.sushishop.shared.exception.core.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductEnrichmentService enrichmentService;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getAll(Pageable pageable, String search, Category category, Boolean available) {
        var spec = Specification.where(ProductSpecification.hasSearch(search))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.isAvailable(available));

        var page = productRepository.findAll(spec, pageable);
        List<Product> products = page.getContent();

        if (products.isEmpty()) {
            return Page.empty(pageable);
        }

        var enriched = enrichProducts(products);
        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getPopular() {
        List<Product> products = productRepository.findPopular(Pageable.ofSize(10));
        return enrichProducts(products);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getRelated(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        List<Product> relatedProducts = productRepository.findRelated(product.getCategory(), id)
                .stream()
                .limit(4)
                .toList();

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