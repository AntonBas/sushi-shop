package com.sushishop.service;

import com.sushishop.annotation.Auditable;
import com.sushishop.domain.Promotion;
import com.sushishop.dto.request.CreatePromotionRequest;
import com.sushishop.dto.response.PromotionResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.PromotionMapper;
import com.sushishop.repository.ProductRepository;
import com.sushishop.repository.PromotionRepository;
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
    private final PromotionMapper promotionMapper;

    @Auditable(action = "CREATE", entity = "Promotion")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "promotions", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    public PromotionResponse create(CreatePromotionRequest request) {
        validateDates(request.startDate(), request.endDate());
        validateProductsExist(request.productIds());

        var products = new HashSet<>(productRepository.findAllById(request.productIds()));

        var promotion = Promotion.builder()
                .title(request.title())
                .description(request.description())
                .discountPercent(request.discountPercent())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .products(products)
                .build();

        var saved = promotionRepository.save(promotion);
        log.info("Promotion created: {}", saved.getId());
        return promotionMapper.toResponse(saved);
    }

    @Cacheable(value = "promotions", key = "'active'")
    public List<PromotionResponse> getActive() {
        var now = LocalDateTime.now();
        return promotionRepository.findByStartDateBeforeAndEndDateAfter(now, now)
                .stream()
                .map(promotionMapper::toResponse)
                .toList();
    }

    public Page<PromotionResponse> getAll(Pageable pageable) {
        return promotionRepository.findAll(pageable).map(promotionMapper::toResponse);
    }

    @Auditable(action = "DELETE", entity = "Promotion")
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

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("End date cannot be in the past");
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