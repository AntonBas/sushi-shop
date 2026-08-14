package com.sushishop.promotion;

import com.sushishop.annotation.Auditable;
import com.sushishop.product.ProductRepository;
import com.sushishop.promotion.dto.request.CreatePromotionRequest;
import com.sushishop.promotion.dto.request.UpdatePromotionRequest;
import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.shared.enums.AuditAction;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionValidator validator;
    private final PromotionResponseAssembler assembler;
    private final SlugService slugService;

    @Auditable(action = AuditAction.CREATE, entity = "Promotion")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "promotions", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    public PromotionResponse create(CreatePromotionRequest request) {
        validator.validateDates(request.startDate(), request.endDate());
        validator.validateTitleUnique(request.title());
        validator.validateProductsExist(request.productIds());
        validator.validateProductsNotInOtherActivePromotions(request.productIds(), null);

        var products = new HashSet<>(productRepository.findAllById(request.productIds()));

        var promotion = Promotion.builder()
                .title(request.title())
                .slug(slugService.generateUniqueSlug(request.title(),
                        slug -> promotionRepository.findBySlug(slug).isPresent()))
                .description(request.description())
                .discountPercent(request.discountPercent())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .products(products)
                .active(true)
                .build();

        var saved = promotionRepository.save(promotion);
        log.info("Promotion created: id={}, title={}", saved.getId(), saved.getTitle());
        return assembler.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActive() {
        return promotionRepository.findActiveAt(LocalDateTime.now())
                .stream()
                .map(assembler::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PromotionResponse> getAll(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return promotionRepository.findAllBySearch(search, pageable).map(assembler::toResponse);
        }
        return promotionRepository.findAll(pageable).map(assembler::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "promotions", key = "#id")
    public PromotionResponse getById(Long id) {
        return promotionRepository.findById(id)
                .map(assembler::toResponse)
                .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "promotions", key = "#slug")
    public PromotionResponse getBySlug(String slug) {
        return promotionRepository.findBySlug(slug)
                .map(assembler::toResponse)
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

        updateTitle(promotion, request.title());
        updateDescription(promotion, request.description());
        updateDiscountPercent(promotion, request.discountPercent());
        updateDates(promotion, request.startDate(), request.endDate());
        updateProducts(promotion, request.productIds());
        updateActive(promotion, request.active());

        var saved = promotionRepository.save(promotion);
        log.info("Promotion updated: id={}, title={}", saved.getId(), saved.getTitle());
        return assembler.toResponse(saved);
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

        promotion.getProducts().clear();
        promotionRepository.save(promotion);
        promotionRepository.delete(promotion);
        log.info("Promotion deleted: id={}", id);
    }

    private void updateTitle(Promotion promotion, String newTitle) {
        if (newTitle != null && !newTitle.equals(promotion.getTitle())) {
            validator.validateTitleUnique(newTitle);
            promotion.setTitle(newTitle);
            promotion.setSlug(slugService.generateUniqueSlug(newTitle,
                    slug -> promotionRepository.findBySlug(slug).isPresent()));
        }
    }

    private void updateDescription(Promotion promotion, String newDescription) {
        if (newDescription != null) {
            promotion.setDescription(newDescription);
        }
    }

    private void updateDiscountPercent(Promotion promotion, java.math.BigDecimal newDiscountPercent) {
        if (newDiscountPercent != null) {
            promotion.setDiscountPercent(newDiscountPercent);
        }
    }

    private void updateDates(Promotion promotion, LocalDateTime newStartDate, LocalDateTime newEndDate) {
        if (newStartDate != null || newEndDate != null) {
            var effectiveStart = newStartDate != null ? newStartDate : promotion.getStartDate();
            var effectiveEnd = newEndDate != null ? newEndDate : promotion.getEndDate();
            validator.validateDates(effectiveStart, effectiveEnd);
            promotion.setStartDate(effectiveStart);
            promotion.setEndDate(effectiveEnd);
        }
    }

    private void updateProducts(Promotion promotion, List<Long> newProductIds) {
        if (newProductIds != null) {
            validator.validateProductsExist(newProductIds);
            validator.validateProductsNotInOtherActivePromotions(newProductIds, promotion.getId());
            promotion.setProducts(new HashSet<>(productRepository.findAllById(newProductIds)));
        }
    }

    private void updateActive(Promotion promotion, Boolean newActive) {
        if (newActive != null) {
            promotion.setActive(newActive);
        }
    }
}