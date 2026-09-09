package com.sushishop.product;

import com.sushishop.audit.Auditable;
import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductResponse;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.SlugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SlugService slugService;
    private final ProductEnrichmentService enrichmentService;
    private final ProductImageService productImageService;
    private final ProductCacheService productCacheService;

    @Auditable(action = AuditAction.CREATE, entity = "Product")
    @Transactional
    public ProductResponse create(CreateProductRequest request, List<MultipartFile> images) {
        if (request.category() == Category.SET && request.pieces() == null) {
            throw new BadRequestException("Pieces is required for sets");
        }

        var product = productMapper.toEntity(request);
        product.setSlug(slugService.generateUniqueSlug(request.name(),
                slug -> productRepository.findBySlug(slug).isPresent()));
        productImageService.addImagesToProduct(product, images);

        var saved = productRepository.save(product);
        log.info("Created product with ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'id:' + #id")
    public ProductResponse getById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with ID: " + id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'slug:' + #slug")
    public ProductResponse getBySlug(String slug) {
        var product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + slug));
        return toResponse(product);
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Product")
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        var oldSlug = product.getSlug();

        if (request.name() != null && !request.name().equals(product.getName())) {
            product.setSlug(slugService.generateUniqueSlug(request.name(),
                    slug -> productRepository.findBySlug(slug).isPresent()));
        }

        productMapper.updateEntity(request, product);

        var updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());

        productCacheService.evict(id, oldSlug);
        if (!oldSlug.equals(updated.getSlug())) {
            productCacheService.evict(id, updated.getSlug());
        }
        return toResponse(updated);
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Product")
    @Transactional
    public void toggleAvailability(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setAvailable(!product.isAvailable());
        productRepository.save(product);
        productCacheService.evict(id, product.getSlug());
        log.info("Product {} is now {}", id, product.isAvailable() ? "available" : "unavailable");
    }

    @Auditable(action = AuditAction.DELETE, entity = "Product")
    @Transactional
    public void delete(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        var slug = product.getSlug();

        product.getProductImages().forEach(img -> productImageService.deleteImage(id, img.getId()));
        product.getPromotions().clear();
        productRepository.save(product);
        productRepository.delete(product);
        productCacheService.evict(id, slug);
        log.debug("Deleted product with ID: {}", id);
    }

    private ProductResponse toResponse(Product product) {
        var base = productMapper.toResponse(product);
        var discount = enrichmentService.calculateDiscountedPrice(product);
        var discountPercent = enrichmentService.getDiscountPercent(product);
        var promotionTitle = enrichmentService.getPromotionTitle(product);
        var rating = enrichmentService.getAverageRatings(List.of(product.getId())).get(product.getId());
        var reviewCount = product.getReviews() != null ? product.getReviews().size() : 0;

        return new ProductResponse(
                base.id(), base.slug(), base.name(), base.description(),
                base.price(), discount, discountPercent, promotionTitle,
                base.category(), base.images(), reviewCount, rating,
                base.available(), base.weight(), base.pieces()
        );
    }
}