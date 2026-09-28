package com.sushishop.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file);

    Resource load(String fileName);

    String determineContentType(String fileName);

    void delete(String fileUrl);
}
