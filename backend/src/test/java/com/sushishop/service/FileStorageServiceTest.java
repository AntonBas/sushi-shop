package com.sushishop.service;

import com.sushishop.exception.core.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        fileStorageService = new FileStorageService();

        var uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(fileStorageService, tempDir.toString());

        var allowedTypesField = FileStorageService.class.getDeclaredField("allowedTypes");
        allowedTypesField.setAccessible(true);
        allowedTypesField.set(fileStorageService, Set.of("image/jpeg", "image/png", "image/webp"));

        var maxFileSizeField = FileStorageService.class.getDeclaredField("maxFileSize");
        maxFileSizeField.setAccessible(true);
        maxFileSizeField.set(fileStorageService, 5242880L);

        var urlPrefixField = FileStorageService.class.getDeclaredField("urlPrefix");
        urlPrefixField.setAccessible(true);
        urlPrefixField.set(fileStorageService, "/api/files/");

        fileStorageService.init();
    }

    @Test
    void shouldStoreFile() {
        var file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test".getBytes());

        var result = fileStorageService.store(file);

        assertThat(result).startsWith("/api/files/");
        assertThat(result).endsWith(".jpg");
    }

    @Test
    void shouldReturnNullForNullFile() {
        var result = fileStorageService.store(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForEmptyFile() {
        var file = new MockMultipartFile("empty.jpg", "empty.jpg", "image/jpeg", new byte[0]);

        var result = fileStorageService.store(file);

        assertThat(result).isNull();
    }

    @Test
    void shouldThrowForInvalidType() {
        var file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "test".getBytes());

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File type not allowed");
    }

    @Test
    void shouldThrowForTooLargeFile() {
        var bytes = new byte[6_000_000];
        var file = new MockMultipartFile("large.jpg", "large.jpg", "image/jpeg", bytes);

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File size exceeds maximum allowed size");
    }
}