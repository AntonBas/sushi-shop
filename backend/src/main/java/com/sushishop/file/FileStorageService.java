package com.sushishop.file;

import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.InternalServerException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.allowed-types:image/jpeg,image/png,image/webp}")
    private Set<String> allowedTypes;

    @Value("${app.upload.max-size:5242880}")
    private long maxFileSize;

    @Value("${app.upload.url-prefix:/api/files/}")
    private String urlPrefix;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new InternalServerException("Could not create upload directory: " + uploadDir, e);
        }
    }

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
            String extension = getExtension(contentType);
            String fileName = UUID.randomUUID() + extension;
            Path filePath = resolveWithinUploadDir(fileName);
            if (filePath == null) {
                throw new BadRequestException("Invalid file path");
            }

            Files.write(filePath, file.getBytes());

            log.info("File stored: {}", fileName);
            return urlPrefix + fileName;
        } catch (IOException e) {
            throw new InternalServerException("Failed to store file", e);
        }
    }

    public Resource load(String fileName) {
        Path filePath = resolveWithinUploadDir(fileName);
        if (filePath == null) {
            log.warn("Attempt to access file outside upload directory: {}", fileName);
            throw new BadRequestException("Invalid file path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return null;
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file path");
        }
    }

    public String determineContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            log.warn("Invalid file URL for deletion: {}", fileUrl);
            return;
        }

        String fileName = fileUrl.substring(urlPrefix.length());
        Path filePath = resolveWithinUploadDir(fileName);
        if (filePath == null) {
            log.warn("Attempt to delete file outside upload directory: {}", fileName);
            return;
        }

        try {
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName, e);
        }
    }

    private Path resolveWithinUploadDir(String fileName) {
        Path filePath = uploadPath.resolve(fileName).normalize();
        return filePath.startsWith(uploadPath) ? filePath : null;
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}