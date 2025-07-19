package com.renee328.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String saveImage(MultipartFile file);
    void cleanupTempImages(List<String> unusedImages);
    void cleanupUnusedImages(Long postId, List<String> usedImages);

}
