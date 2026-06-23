package com.sushishop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        fileStorageService = new FileStorageService();
        var field = FileStorageService.class.getDeclaredField("uploadDir");
        field.setAccessible(true);
        field.set(fileStorageService, tempDir.toString());
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
    void shouldReturnNullForEmptyFile() {
        var result = fileStorageService.store(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldThrowForInvalidType() {
        var file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "test".getBytes());

        assertThatThrownBy(() -> fileStorageService.store(file)).isInstanceOf(RuntimeException.class).hasMessageContaining("File type not allowed");
    }
}