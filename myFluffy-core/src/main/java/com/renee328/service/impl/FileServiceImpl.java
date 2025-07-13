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
import java.util.HashSet;
import java.util.List;
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

        return baseUrl + "/" + datePath + "/" + savedFileName;
    }

    @Override
    @Transactional
    public void cleanupUnusedImages(Long postId, String contentHtml) {
        if (contentHtml == null) return;

        // 본문에서 추출된 이미지 경로
        Set<String> usedImagePaths = new HashSet<>();
        Matcher matcher = Pattern.compile("/uploads/images/(\\d{4}/\\d{2}/\\d{2})/([^\"'>]+)").matcher(contentHtml);
        while (matcher.find()) {
            String datePath = matcher.group(1);
            String filename = matcher.group(2);
            usedImagePaths.add(datePath + "/" + filename);
        }

        // 해당 게시글에 저장된 이미지 경로 데이터베이스에서 가져오기 - 동일한 날짜의 다른 게시글 이미지 삭제 방지
        List<String> dbImagePaths = postImageMapper.getImageUrlsByPostId(postId); // 경로 예: uploads/images/2025/07/03/xxx.jpg

        for (String fullPath : dbImagePaths) {
            String relativePath = fullPath.replaceFirst("^/uploads/images/", "");
            if (!usedImagePaths.contains(relativePath)) {
                File file = new File(baseDir + "/" + relativePath);
                log.debug("[IMAGE CLEANUP] 삭제하려는 이미지 파일 서버 경로 : {}", file.getAbsolutePath());
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.debug("[IMAGE CLEANUP] 사용하지 않는 이미지 삭제 성공: {}", file.getPath());
                    } else {
                        log.debug("[IMAGE CLEANUP] 사용하지 않는 이미지 삭제 실패: {}", file.getPath());
                    }
                }
            }
        }
    }
}
