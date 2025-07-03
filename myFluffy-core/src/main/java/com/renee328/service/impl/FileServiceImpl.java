package com.renee328.service.impl;

import com.renee328.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.base-dir}")
    private String baseDir;

    @Value("${file.upload.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
        }

        LocalDate today = LocalDate.now();
        String datePath = today.getYear() + "/" + String.format("%02d", today.getMonthValue()) + "/" + String.format("%02d", today.getDayOfMonth());
        String uploadPath = baseDir + "/" + datePath;

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFileName = UUID.randomUUID().toString() + extension;
        File destination = new File(uploadPath, savedFileName);

        try {
            file.transferTo(destination);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }

        return baseUrl + "/" + datePath + "/" + savedFileName;
    }

    @Override
    @Transactional
    public void cleanupUnusedImages(String contentHtml) {
        if (contentHtml == null) return;

        // HTML에서 사용된 이미지 경로 추출
        Set<String> usedImageFilenames = new HashSet<>();
        Set<String> datePathsToCheck = new HashSet<>();

        Matcher matcher = Pattern.compile("/uploads/images/(\\d{4}/\\d{2}/\\d{2})/([^\"'>]+)").matcher(contentHtml);
        while (matcher.find()) {
            String datePath = matcher.group(1);
            String filename = matcher.group(2);
            usedImageFilenames.add(datePath + "/" + filename);
            datePathsToCheck.add(datePath);
        }

        for (String datePath : datePathsToCheck) {
            String imageDirPath = baseDir + "/" + datePath;
            File imageDir = new File(imageDirPath);

            if (imageDir.exists() && imageDir.isDirectory()) {
                File[] files = imageDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String relativePath = datePath + "/" + file.getName();
                        if (!usedImageFilenames.contains(relativePath)) {
                            boolean deleted = file.delete(); // 본문에 포함되지 않은 이미지 삭제
                            if (deleted) {
                                System.out.println("[IMAGE CLEANUP] deleted file: " + file.getPath());
                            }
                        }
                    }
                }
            }
        }
    }
}
