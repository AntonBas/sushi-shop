package com.sushishop.service;

import com.sushishop.domain.Promotion;
import com.sushishop.dto.request.CreatePromotionRequest;
import com.sushishop.dto.response.PromotionResponse;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.PromotionMapper;
import com.sushishop.repository.ProductRepository;
import com.sushishop.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final PromotionMapper promotionMapper;

    @CacheEvict(value = "promotions", allEntries = true)
    public PromotionResponse create(CreatePromotionRequest request) {
        var promotion = toEntity(request);
        var saved = promotionRepository.save(promotion);
        log.info("Promotion created: {}", saved.getId());
        return promotionMapper.toResponse(saved);
    }

    @Cacheable("promotions")
    public List<PromotionResponse> getActive() {
        return toResponseList(promotionRepository.findByActiveTrueAndEndDateAfter(LocalDateTime.now()));
    }

    @Cacheable("promotions")
    public List<PromotionResponse> getAll() {
        return toResponseList(promotionRepository.findAll());
    }

    @CacheEvict(value = "promotions", allEntries = true)
    public void delete(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new NotFoundException("Promotion not found: " + id);
        }
        promotionRepository.deleteById(id);
        log.info("Promotion deleted: {}", id);
    }

    private Promotion toEntity(CreatePromotionRequest request) {
        return Promotion.builder()
                .title(request.title())
                .description(request.description())
                .discountPercent(request.discountPercent())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .products(productRepository.findAllById(request.productIds()))
                .build();
    }

    private List<PromotionResponse> toResponseList(List<Promotion> promotions) {
        return promotions.stream()
                .map(promotionMapper::toResponse)
                .toList();
    }
}