package com.sushishop.file;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.InternalServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CloudinaryFileStorageServiceTest {

    private Cloudinary cloudinary;
    private Uploader uploader;
    private CloudinaryFileStorageService service;

    @BeforeEach
    public void setUp() {
        cloudinary = Mockito.mock(Cloudinary.class);
        uploader = Mockito.mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        service = new CloudinaryFileStorageService(cloudinary);
        ReflectionTestUtils.setField(service, "allowedTypes", Set.of("image/jpeg", "image/png", "image/webp"));
        ReflectionTestUtils.setField(service, "maxFileSize", 5_242_880L);
    }

    @Test
    public void shouldStoreFileAndReturnSecureUrl() throws IOException {
        var file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test".getBytes());
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v1699999999/products/abc123.jpg"));

        var result = service.store(file);

        assertThat(result).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1699999999/products/abc123.jpg");
    }

    @Test
    public void shouldReturnNullForNullFile() {
        assertThat(service.store(null)).isNull();
    }

    @Test
    public void shouldReturnNullForEmptyFile() {
        var file = new MockMultipartFile("empty.jpg", "empty.jpg", "image/jpeg", new byte[0]);
        assertThat(service.store(file)).isNull();
    }

    @Test
    public void shouldThrowForInvalidType() {
        var file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "test".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File type not allowed");
    }

    @Test
    public void shouldThrowForTooLargeFile() {
        var file = new MockMultipartFile("large.jpg", "large.jpg", "image/jpeg", new byte[6_000_000]);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File size exceeds maximum allowed size");
    }

    @Test
    public void shouldWrapUploadIOException() throws IOException {
        var file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("network down"));

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(InternalServerException.class);
    }

    @Test
    public void shouldDestroyByExtractedPublicId() throws IOException {
        service.delete("https://res.cloudinary.com/demo/image/upload/v1699999999/products/abc123.jpg");

        verify(uploader).destroy(eq("products/abc123"), anyMap());
    }

    @Test
    public void shouldNotThrowWhenDestroyFails() throws IOException {
        when(uploader.destroy(any(), anyMap())).thenThrow(new IOException("network down"));

        service.delete("https://res.cloudinary.com/demo/image/upload/v1699999999/products/abc123.jpg");
    }

    @Test
    public void shouldSkipDeletionForInvalidUrl() throws IOException {
        service.delete("not-a-cloudinary-url");

        verify(uploader, Mockito.never()).destroy(any(), anyMap());
    }

    @ParameterizedTest
    @CsvSource({
            "https://res.cloudinary.com/demo/image/upload/v1699999999/products/abc123.jpg, products/abc123",
            "https://res.cloudinary.com/demo/image/upload/products/abc123.png, products/abc123",
            "https://res.cloudinary.com/demo/image/upload/v1/abc123.jpeg, abc123",
    })
    public void shouldExtractPublicIdFromVariousUrlShapes(String url, String expectedPublicId) {
        assertThat(CloudinaryFileStorageService.extractPublicId(url)).isEqualTo(expectedPublicId);
    }

    @Test
    public void shouldReturnNullPublicIdForMalformedUrl() {
        assertThat(CloudinaryFileStorageService.extractPublicId("https://example.com/no-upload-segment.jpg")).isNull();
        assertThat(CloudinaryFileStorageService.extractPublicId(null)).isNull();
    }
}
