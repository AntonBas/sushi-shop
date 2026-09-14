package com.sushishop.file;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class CloudinaryFileStorageService implements FileStorageService {

    private static final Pattern VERSION_SEGMENT = Pattern.compile("^v\\d+/");

    private final Cloudinary cloudinary;

    @Value("${app.upload.allowed-types:image/jpeg,image/png,image/webp}")
    private Set<String> allowedTypes;

    @Value("${app.upload.max-size:5242880}")
    private long maxFileSize;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BadRequestException("File type not allowed: " + contentType);
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "products"));
            String url = (String) result.get("secure_url");
            log.info("File uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            throw new InternalServerException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public Resource load(String fileName) {
        // Cloudinary URLs are absolute and public — the browser fetches them
        // directly, never through this backend. Nothing to serve here.
        return null;
    }

    @Override
    public String determineContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    @Override
    public void delete(String fileUrl) {
        String publicId = extractPublicId(fileUrl);
        if (publicId == null) {
            log.warn("Invalid Cloudinary URL for deletion: {}", fileUrl);
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
        }
    }

    static String extractPublicId(String secureUrl) {
        if (secureUrl == null) {
            return null;
        }

        int uploadIndex = secureUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }

        String afterUpload = secureUrl.substring(uploadIndex + "/upload/".length());
        afterUpload = VERSION_SEGMENT.matcher(afterUpload).replaceFirst("");

        int lastDot = afterUpload.lastIndexOf('.');
        return lastDot == -1 ? afterUpload : afterUpload.substring(0, lastDot);
    }
}
