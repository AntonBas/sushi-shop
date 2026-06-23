package com.sushishop.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(CreateProductRequest request) {
        log.info("Create product request: {}", request.name());
        var product = productMapper.toEntity(request);
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

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void delete(Long id) {
        log.info("Delete product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);

        log.debug("Deleted product with ID: {}", id);
    }
}
