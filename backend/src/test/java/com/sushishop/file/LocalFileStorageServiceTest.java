package com.sushishop.file;

import com.sushishop.shared.exception.core.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    private LocalFileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalFileStorageService();

        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileStorageService, "allowedTypes", Set.of("image/jpeg", "image/png", "image/webp"));
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", 5242880L);
        ReflectionTestUtils.setField(fileStorageService, "urlPrefix", "/api/files/");

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

    @Test
    void shouldLoadFile() throws Exception {
        var fileName = "test-load.jpg";
        var filePath = tempDir.resolve(fileName);
        Files.write(filePath, "test".getBytes());

        var resource = fileStorageService.load(fileName);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void shouldReturnNullForNonExistentFile() {
        var resource = fileStorageService.load("nonexistent.jpg");
        assertThat(resource).isNull();
    }

    @Test
    void shouldThrowForPathTraversal() {
        assertThatThrownBy(() -> fileStorageService.load("../../etc/passwd"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid file path");
    }

    @Test
    void shouldDeleteFile() throws Exception {
        var fileName = "test-delete.jpg";
        var filePath = tempDir.resolve(fileName);
        Files.write(filePath, "test".getBytes());

        fileStorageService.delete("/api/files/" + fileName);

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void shouldNotThrowWhenDeleteNonExistentFile() {
        fileStorageService.delete("/api/files/nonexistent.jpg");
    }

    @Test
    void shouldNotDeleteWithInvalidUrl() throws Exception {
        var fileName = "test-invalid.jpg";
        var filePath = tempDir.resolve(fileName);
        Files.write(filePath, "test".getBytes());

        fileStorageService.delete("invalid-url");

        assertThat(Files.exists(filePath)).isTrue();
    }
}