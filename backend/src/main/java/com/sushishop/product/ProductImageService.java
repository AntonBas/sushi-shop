package com.sushishop.product;

import com.sushishop.file.FileStorageService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.TransactionCallbacks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    static final int MAX_IMAGES_PER_UPLOAD = 5;

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final ProductCacheService productCacheService;

    @Transactional
    public void addImage(Long productId, MultipartFile file) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        String url = fileStorageService.store(file);
        if (url == null) {
            log.warn("Skipping empty image for product: {}", productId);
            return;
        }
        TransactionCallbacks.afterRollback(() -> fileStorageService.delete(url));

        int nextOrder = product.getProductImages().stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        product.getProductImages().add(ProductImage.builder()
                .url(url)
                .sortOrder(nextOrder)
                .product(product)
                .build());
        productRepository.save(product);
        productCacheService.evict(productId, product.getSlug());
        log.info("Image added to product: {}", productId);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        var image = product.getProductImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Image not found: " + imageId));

        var url = image.getUrl();
        product.getProductImages().remove(image);
        productRepository.save(product);
        productCacheService.evict(productId, product.getSlug());
        TransactionCallbacks.afterCommit(() -> fileStorageService.delete(url));
        log.info("Image {} deleted from product: {}", imageId, productId);
    }

    @Transactional
    public void reorderImages(Long productId, List<Long> imageIds) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        for (int i = 0; i < imageIds.size(); i++) {
            final int newOrder = i;
            product.getProductImages().stream()
                    .filter(img -> img.getId().equals(imageIds.get(newOrder)))
                    .findFirst()
                    .ifPresent(img -> img.setSortOrder(newOrder));
        }
        productRepository.save(product);
        productCacheService.evict(productId, product.getSlug());
        log.info("Images reordered for product: {}", productId);
    }

    public void addImagesToProduct(Product product, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        if (images.size() > MAX_IMAGES_PER_UPLOAD) {
            throw new BadRequestException("You can upload at most " + MAX_IMAGES_PER_UPLOAD + " images at once");
        }

        List<ProductImage> productImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            String url = fileStorageService.store(images.get(i));
            if (url != null) {
                TransactionCallbacks.afterRollback(() -> fileStorageService.delete(url));
                productImages.add(ProductImage.builder()
                        .url(url)
                        .sortOrder(i)
                        .product(product)
                        .build());
            }
        }
        product.setProductImages(productImages);
    }
}