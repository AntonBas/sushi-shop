package com.sushishop.service;

import com.sushishop.annotation.Auditable;
import com.sushishop.domain.Product;
import com.sushishop.domain.ProductImage;
import com.sushishop.domain.Promotion;
import com.sushishop.domain.Review;
import com.sushishop.domain.enums.Category;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.ProductMapper;
import com.sushishop.repository.ProductRepository;
import com.sushishop.repository.ProductSpecs;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Auditable(action = "CREATE", entity = "Product")
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(CreateProductRequest request, List<MultipartFile> images) {
        log.info("Creating product with {} images: {}", images != null ? images.size() : 0, request.name());
        var product = productMapper.toEntity(request);
        addImagesToProduct(product, images);

        if (request.category() == Category.SET && request.pieces() == null) {
            throw new BadRequestException("Pieces is required for sets");
        }

        var saved = productRepository.save(product);
        log.info("Created product with ID: {}", saved.getId());
        return enrichProductResponse(saved);
    }

    @Cacheable("products")
    public Page<ProductListResponse> getAll(Pageable pageable, String search, Category category, Boolean available) {
        var spec = Specification.where(ProductSpecs.hasSearch(search)).and(ProductSpecs.hasCategory(category)).and(ProductSpecs.isAvailable(available));
        log.info("Getting all products, page: {}", pageable.getPageNumber());
        return productRepository.findAll(spec, pageable).map(this::enrichListResponse);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(Long id) {
        log.info("Get product with ID: {}", id);
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return enrichProductResponse(product);
    }

    @Cacheable(value = "products", key = "popular")
    public List<ProductListResponse> getPopular() {
        return productRepository.findPopular(Pageable.ofSize(10))
                .stream()
                .map(this::enrichListResponse)
                .toList();
    }

    @Cacheable(value = "products", key = "'related-' + #id")
    public List<ProductListResponse> getRelated(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return productRepository.findRelated(product.getCategory(), id, Pageable.ofSize(4))
                .stream()
                .map(this::enrichListResponse)
                .toList();
    }

    @Auditable(action = "UPDATE", entity = "Product")
    @CachePut(value = "products", key = "#id")
    public ProductResponse update(Long id, UpdateProductRequest request) {
        log.info("Update product : {}", request.name());
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        productMapper.updateEntity(request, product);
        var updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());
        return enrichProductResponse(updated);
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

    @Auditable(action = "TOGGLE", entity = "Product")
    @CacheEvict(value = "products", key = "#id")
    public void toggleAvailability(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setAvailable(!product.isAvailable());
        productRepository.save(product);
        log.info("Product {} is now {}", id, product.isAvailable() ? "available" : "unavailable");
    }

    @Auditable(action = "DELETE", entity = "Product")
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

    private Promotion getBestPromo(Product product) {
        return product.getPromotions().stream()
                .filter(p -> p.isActive() && p.getEndDate().isAfter(LocalDateTime.now()))
                .max(Comparator.comparing(Promotion::getDiscountPercent))
                .orElse(null);
    }

    private Double getAverageRating(Product product) {
        if (product.getReviews().isEmpty()) return null;
        return product.getReviews().stream()
                .mapToInt(Review::getRating).average().orElse(0.0);
    }

    private ProductListResponse enrichListResponse(Product product) {
        var response = productMapper.toListResponse(product);
        var bestPromo = getBestPromo(product);

        BigDecimal discountedPrice = null;
        if (bestPromo != null) {
            var discount = bestPromo.getDiscountPercent().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountedPrice = product.getPrice().multiply(BigDecimal.ONE.subtract(discount));
        }

        String mainImage = product.getProductImages().stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl).orElse(null);

        return new ProductListResponse(
                response.id(), response.name(), response.price(), discountedPrice,
                getAverageRating(product), response.category(), mainImage, response.available(),
                product.getWeight(), product.getPieces()
        );
    }

    private ProductResponse enrichProductResponse(Product product) {
        var response = productMapper.toResponse(product);
        var bestPromo = getBestPromo(product);

        BigDecimal discountedPrice = null;
        BigDecimal discountPercent = null;
        String promotionTitle = null;
        if (bestPromo != null) {
            discountPercent = bestPromo.getDiscountPercent();
            promotionTitle = bestPromo.getTitle();
            var discount = discountPercent.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountedPrice = product.getPrice().multiply(BigDecimal.ONE.subtract(discount));
        }

        List<String> images = product.getProductImages().stream()
                .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl).toList();

        int reviewCount = product.getReviews().size();

        return new ProductResponse(
                response.id(), response.name(), response.description(),
                response.price(), discountedPrice, discountPercent, promotionTitle,
                response.category(), images, reviewCount, getAverageRating(product), response.available(),
                product.getWeight(), product.getPieces()
        );
    }
}