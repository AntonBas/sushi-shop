package com.sushishop.product;

import com.sushishop.annotation.Auditable;
import com.sushishop.file.FileStorageService;
import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.enums.Category;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;
    private final SlugService slugService;
    private final ProductEnrichmentService productEnrichmentService;

    @Auditable(action = AuditAction.CREATE, entity = "Product")
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(CreateProductRequest request, List<MultipartFile> images) {
        log.info("Creating product with {} images: {}", images != null ? images.size() : 0, request.name());
        var product = productMapper.toEntity(request);
        product.setSlug(slugService.generateUniqueSlug(request.name()));
        addImagesToProduct(product, images);

        if (request.category() == Category.SET && request.pieces() == null) {
            throw new BadRequestException("Pieces is required for sets");
        }

        var saved = productRepository.save(product);
        log.info("Created product with ID: {}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Cacheable("products")
    public Page<ProductListResponse> getAll(Pageable pageable, String search, Category category, Boolean available) {
        var spec = Specification.where(ProductSpecification.hasSearch(search))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.isAvailable(available));
        log.info("Getting all products, page: {}", pageable.getPageNumber());
        var page = productRepository.findAll(spec, pageable);

        var productIds = page.getContent().stream().map(Product::getId).toList();
        var ratings = productEnrichmentService.getAverageRatings(productIds);

        return page.map(product -> {
            var response = productMapper.toListResponse(product);
            return new ProductListResponse(
                    response.id(), response.slug(), response.name(), response.price(),
                    response.discountedPrice(), ratings.get(product.getId()),
                    response.category(), response.mainImage(), response.available(),
                    response.weight(), response.pieces()
            );
        });
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(Long id) {
        log.info("Get product with ID: {}", id);
        return productRepository.findById(id).map(productMapper::toResponse).orElseThrow(() -> new NotFoundException("Product with ID: " + id));
    }

    @Cacheable(value = "products", key = "#slug")
    public ProductResponse getBySlug(String slug) {
        log.info("Get product by slug: {}", slug);
        return productRepository.findBySlug(slug).map(productMapper::toResponse).orElseThrow(() -> new NotFoundException("Product not found: " + slug));
    }

    @Cacheable(value = "products", key = "'popular'")
    public List<ProductListResponse> getPopular() {
        return productRepository.findPopular(Pageable.ofSize(10))
                .stream()
                .map(productMapper::toListResponse)
                .toList();
    }

    @Cacheable(value = "products", key = "'related-' + #id")
    public List<ProductListResponse> getRelated(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return productRepository.findRelated(product.getCategory(), id)
                .stream()
                .limit(4)
                .map(productMapper::toListResponse)
                .toList();
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Product")
    @CachePut(value = "products", key = "#id")
    public ProductResponse update(Long id, UpdateProductRequest request) {
        log.info("Update product : {}", request.name());
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        if (request.name() != null && !request.name().equals(product.getName())) {
            product.setSlug(slugService.generateUniqueSlug(request.name()));
        }

        productMapper.updateEntity(request, product);

        var updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());
        return productMapper.toResponse(updated);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void addImage(Long productId, MultipartFile file) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        String url = fileStorageService.store(file);
        if (url == null) {
            log.warn("Skipping empty image for product: {}", productId);
            return;
        }
        int nextOrder = product.getProductImages().stream()
                .mapToInt(ProductImage::getSortOrder).max().orElse(-1) + 1;
        product.getProductImages().add(ProductImage.builder()
                .url(url).sortOrder(nextOrder).product(product).build());
        productRepository.save(product);
        log.info("Image added to product: {}", productId);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void deleteImage(Long productId, Long imageId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        var image = product.getProductImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Image not found: " + imageId));
        fileStorageService.delete(image.getUrl());
        product.getProductImages().remove(image);
        productRepository.save(product);
        log.info("Image {} deleted from product: {}", imageId, productId);
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Product")
    @CacheEvict(value = "products", key = "#id")
    public void toggleAvailability(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setAvailable(!product.isAvailable());
        productRepository.save(product);
        log.info("Product {} is now {}", id, product.isAvailable() ? "available" : "unavailable");
    }

    @Auditable(action = AuditAction.DELETE, entity = "Product")
    @Caching(evict = {@CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)})
    public void delete(Long id) {
        log.info("Delete product with ID: {}", id);
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.getProductImages().forEach(img -> fileStorageService.delete(img.getUrl()));
        productRepository.delete(product);
        log.debug("Deleted product with ID: {}", id);
    }

    private void addImagesToProduct(Product product, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        List<ProductImage> productImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String url = fileStorageService.store(images.get(i));
            if (url != null) {
                productImages.add(ProductImage.builder().url(url).sortOrder(i).product(product).build());
            }
        }
        product.setProductImages(productImages);
    }
}