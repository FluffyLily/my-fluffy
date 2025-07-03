package com.renee328.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String saveImage(MultipartFile file);
    void cleanupUnusedImages(String contentHtml);
}
