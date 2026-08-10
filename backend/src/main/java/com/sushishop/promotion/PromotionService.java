package com.sushishop.promotion;

import com.sushishop.annotation.Auditable;
import com.sushishop.product.Product;
import com.sushishop.product.ProductEnrichmentService;
import com.sushishop.product.ProductMapper;
import com.sushishop.product.ProductRepository;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.request.UpdatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.SlugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionMapper promotionMapper;
    private final ProductMapper productMapper;
    private final ProductEnrichmentService productEnrichmentService;
    private final SlugService slugService;

    @Auditable(action = AuditAction.CREATE, entity = "Promotion")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "promotions", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    public PromotionResponse create(CreatePromotionRequest request) {
        validateDates(request.startDate(), request.endDate());
        validateTitleUnique(request.title());
        validateProductsExist(request.productIds());

        var products = new HashSet<>(productRepository.findAllById(request.productIds()));

        var promotion = Promotion.builder()
                .title(request.title())
                .slug(slugService.generateUniqueSlug(request.title(), slug -> promotionRepository.findBySlug(slug).isPresent()))
                .description(request.description())
                .discountPercent(request.discountPercent())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .products(products)
                .build();

        var saved = promotionRepository.save(promotion);
        log.info("Promotion created: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActive() {
        var now = LocalDateTime.now();
        return promotionRepository.findByStartDateBeforeAndEndDateAfter(now, now)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PromotionResponse> getAll(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return promotionRepository.findAllBySearch(search, pageable).map(this::toResponse);
        }
        return promotionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "promotions", key = "#id")
    public PromotionResponse getById(Long id) {
        return promotionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "promotions", key = "#slug")
    public PromotionResponse getBySlug(String slug) {
        return promotionRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Promotion not found: " + slug));
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Promotion")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "promotions", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    public PromotionResponse update(Long id, UpdatePromotionRequest request) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));

        if (request.title() != null && !request.title().equals(promotion.getTitle())) {
            validateTitleUnique(request.title());
            promotion.setTitle(request.title());
            promotion.setSlug(slugService.generateUniqueSlug(request.title(), slug -> promotionRepository.findBySlug(slug).isPresent()));
        }

        if (request.description() != null) {
            promotion.setDescription(request.description());
        }

        if (request.discountPercent() != null) {
            promotion.setDiscountPercent(request.discountPercent());
        }

        if (request.startDate() != null && request.endDate() != null) {
            validateDates(request.startDate(), request.endDate());
            promotion.setStartDate(request.startDate());
            promotion.setEndDate(request.endDate());
        }

        if (request.productIds() != null) {
            validateProductsExist(request.productIds());
            promotion.setProducts(new HashSet<>(productRepository.findAllById(request.productIds())));
        }

        var saved = promotionRepository.save(promotion);
        log.info("Promotion updated: {}", saved.getId());
        return toResponse(saved);
    }

    @Auditable(action = AuditAction.DELETE, entity = "Promotion")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "promotions", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void delete(Long id) {
        var promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));
        promotionRepository.delete(promotion);
        log.info("Promotion deleted: {}", id);
    }

    private PromotionResponse toResponse(Promotion promotion) {
        List<Long> productIds = promotion.getProducts().stream()
                .map(Product::getId)
                .toList();

        Map<Long, Double> ratings = productEnrichmentService.getAverageRatings(productIds);

        BigDecimal discount = promotion.isActive() && promotion.getEndDate().isAfter(LocalDateTime.now())
                ? promotion.getDiscountPercent().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;

        List<ProductListResponse> productResponses = promotion.getProducts().stream()
                .map(product -> {
                    BigDecimal discountedPrice = discount != null
                            ? product.getPrice().multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP)
                            : null;
                    return productMapper.toListResponse(product, ratings.get(product.getId()), discountedPrice);
                })
                .toList();

        var response = promotionMapper.toResponse(promotion);
        return new PromotionResponse(
                response.id(),
                response.slug(),
                response.title(),
                response.description(),
                response.discountPercent(),
                response.startDate(),
                response.endDate(),
                response.active(),
                productResponses
        );
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("End date cannot be in the past");
        }
    }

    private void validateTitleUnique(String title) {
        if (promotionRepository.findByTitle(title).isPresent()) {
            throw new BadRequestException("Promotion with title '" + title + "' already exists");
        }
    }

    private void validateProductsExist(List<Long> productIds) {
        var products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            var foundIds = products.stream().map(p -> p.getId().toString()).toList();
            var missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id.toString()))
                    .map(String::valueOf)
                    .toList();
            throw new NotFoundException("Products not found: " + String.join(", ", missingIds));
        }
    }
}