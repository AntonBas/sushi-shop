package com.sushishop.service;

import com.sushishop.domain.ProductImage;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.ProductMapper;
import com.sushishop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(CreateProductRequest request, List<MultipartFile> images) {
        log.info("Creating product with {} images: {}", images != null ? images.size() : 0, request.name());
        var product = productMapper.toEntity(request);

        if (images != null && !images.isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                String url = fileStorageService.store(images.get(i));
                productImages.add(ProductImage.builder().url(url).sortOrder(i).product(product).build());
            }
            product.setProductImages(productImages);
        }
        var saved = productRepository.save(product);
        log.info("Created product with ID: {}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Cacheable("products")
    public Page<ProductListResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toListResponse);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getById(Long id) {
        log.info("Get product with ID: {}", id);
        return productRepository.findById(id).map(productMapper::toResponse).orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    @CachePut(value = "products", key = "#id")
    public ProductResponse update(Long id, UpdateProductRequest request) {
        log.info("Update product : {}", request.name());
        var product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found: " + id));
        productMapper.updateEntity(request, product);
        var updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());
        return productMapper.toResponse(updated);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void addImage(Long productId, MultipartFile file) {
        var product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        String url = fileStorageService.store(file);
        int nextOrder = product.getProductImages().stream().mapToInt(ProductImage::getSortOrder).max().orElse(-1) + 1;
        product.getProductImages().add(ProductImage.builder().url(url).sortOrder(nextOrder).product(product).build());
        productRepository.save(product);
        log.info("Image added to product: {}", productId);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void deleteImage(Long productId, Long imageId) {
        var product = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        product.getProductImages().removeIf(img -> img.getId().equals(imageId));
        productRepository.save(product);
        log.info("Image {} deleted from product: {}", imageId, productId);
    }

    @CacheEvict(value = "products", key = "#id")
    public void toggleAvailability(Long id) {
        var product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setAvailable(!product.isAvailable());
        productRepository.save(product);
        log.info("Product {} is now {}", id, product.isAvailable() ? "available" : "unavailable");
    }

    @Caching(evict = {@CacheEvict(value = "products", key = "#id"), @CacheEvict(value = "products", allEntries = true)})
    public void delete(Long id) {
        log.info("Delete product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);

        log.debug("Deleted product with ID: {}", id);
    }
}
