package com.renee328.service.impl;

import com.renee328.mapper.PostImageMapper;
import com.renee328.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.base-dir}")
    private String baseDir;
    private final PostImageMapper postImageMapper;
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    public FileServiceImpl (PostImageMapper postImageMapper) {
        this.postImageMapper = postImageMapper;
    }

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

        return datePath + "/" + savedFileName;
    }

    @Override
    @Transactional
    public void cleanupTempImages(List<String> unusedImages) {
        for (String url : unusedImages) {
            if (url == null || !url.startsWith("/uploads/images/")) continue;

            String relativePath = url.replaceFirst("/uploads/images/", "");
            File file = new File(baseDir + File.separator + relativePath);

            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("[cleanupTempImages - 성공]: {}", file.getAbsolutePath());
                } else {
                    log.warn("[cleanupTempImages - 실패]: {}", file.getAbsolutePath());
                }
            } else {
                log.debug("[cleanupTempImages - 파일 없음]: {}", file.getAbsolutePath());
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
        List<String> dbImagePaths = postImageMapper.getImageUrlsByPostId(postId); // 경로 예: uploads/images/2025/07/03/xxx.jpg

        for (String dbImagePath : dbImagePaths) {
            String relativePath = dbImagePath.replaceFirst("^/uploads/images/", "");

            if (!usedRelPaths.contains(relativePath)) {
                File file = new File(baseDir + "/" + relativePath);

                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info("[cleanupUnusedImages - 성공]: {}", file.getPath());
                    } else {
                        log.info("[cleanupUnusedImages - 실패]: {}", file.getPath());
                    }
                }
            }
        }
    }
}
