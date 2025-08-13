package com.renee328.service.impl;

import com.renee328.mapper.PostImageMapper;
import com.renee328.service.FileService;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.base-dir}")
    private String baseDir;
    @Value("${file.upload.base-url}")
    private String baseUrl;
    private final PostImageMapper postImageMapper;

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    // 업로드 허용 확장자
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    public FileServiceImpl (PostImageMapper postImageMapper) {
        this.postImageMapper = postImageMapper;
    }

    @Override
    @Transactional
    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
        }

        // 파일 확장자 검사
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않은 파일 확장자입니다: " + extension);
        }

        // MIME 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        LocalDate today = LocalDate.now();
        String datePath = today.getYear() + "/" + String.format("%02d", today.getMonthValue()) + "/" + String.format("%02d", today.getDayOfMonth());
        Path uploadPath = Paths.get(baseDir, datePath);

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패", e);
        }

        String savedFileName = UUID.randomUUID().toString() + extension;
        Path destination = uploadPath.resolve(savedFileName);

        try {
            // 1. 저장
            file.transferTo(destination.toFile());

            // 2. Tika MIME 검사
            Tika tika = new Tika();
            String detectedType = tika.detect(destination.toFile());
            if (!detectedType.startsWith("image/")) {
                Files.deleteIfExists(destination);
                throw new IllegalArgumentException("실제 이미지 파일만 업로드할 수 있습니다. (Tika MIME 검사 실패)");
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 또는 검사 중 오류 발생", e);
        }

        String publicUrl = baseUrl + "/" + datePath + "/" + savedFileName;
        return publicUrl;
    }

    @Override
    @Transactional
    public void cleanupTempImages(List<String> unusedImages) {
        for (String url : unusedImages) {
            if (url == null || !url.startsWith("/uploads/images/")) continue;

            String relativePath = url.replaceFirst("/uploads/images/", "");
            Path path = Paths.get(baseDir, relativePath);

            try {
                Files.deleteIfExists(path);
                log.info("[cleanupTempImages - 삭제됨]: {}", path);
            } catch (IOException e) {
                log.warn("[cleanupTempImages - 삭제 실패]: {}, 원인: {}", path, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void cleanupUnusedImages(Long postId, List<String> usedImages) {
        if (usedImages == null) return;

        // 실제 서버 경로 추출
        List<String> usedRelPaths = usedImages.stream()
                .map(p -> p.replaceFirst("^/uploads/images/", ""))
                .toList();
        // 해당 게시글에 저장된 이미지 경로 데이터베이스에서 가져오기
        List<String> dbImagePaths = postImageMapper.getImageUrlsByPostId(postId); // 경로 예: /uploads/images/2025/07/03/xxx.jpg

        for (String dbImagePath : dbImagePaths) {
            String relativePath = dbImagePath.replaceFirst("^/uploads/images/", "");
            if (!usedRelPaths.contains(relativePath)) {
                Path path = Paths.get(baseDir, relativePath);
                try {
                    Files.deleteIfExists(path);
                    log.info("[cleanupUnusedImages - 삭제됨]: {}", path);
                } catch (IOException e) {
                    log.warn("[cleanupUnusedImages - 삭제 실패]: {}, 원인: {}", path, e.getMessage());
                }
            }
        }
    }
}
