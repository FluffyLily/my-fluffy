package com.renee328.admin.controller;

import com.renee328.dto.PostDto;
import com.renee328.dto.PostSearchCondition;
import com.renee328.service.FileService;
import com.renee328.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.renee328.admin.util.ApiConstants.POST_API_URL;

@RestController
@RequestMapping(POST_API_URL)
public class PostController {

    private final PostService postService;
    private final FileService fileService;

    public PostController(PostService postService, FileService fileService) {
        this.postService = postService;
        this.fileService = fileService;
    }

    // 전체 게시글 목록 & 개수 조회
    @PostMapping("/list")
    public Map<String, Object> getPosts(@RequestBody PostSearchCondition searchCondition) {
        List<PostDto> postList = postService.getPostList(searchCondition);
        int totalCount = postService.getPostCount(searchCondition);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postList);
        response.put("totalCount", totalCount);
        return response;
    }

    // 대시보드 전체 게시글 수 / 오늘 게시글 수 조회
    @GetMapping("/count-summary")
    public ResponseEntity<Map<String, Object>> getPostCountSummary() {
        int totalCount = postService.getPostCount(new PostSearchCondition());
        int todayCount = postService.getTodayPostCount();

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        response.put("todayCount", todayCount);
        return ResponseEntity.ok(response);
    }

    // 최근 7일 간의 게시글 수 조회
    @GetMapping("/weekly-count")
    public ResponseEntity<List<Map<String, Object>>> getPostCountLast7Days() {
        List<Map<String, Object>> weeklyCounts = postService.getPostCountLast7Days();
        return ResponseEntity.ok(weeklyCounts);
    }

    // 게시글 세부내용 조회
    @GetMapping("/detail/{postId}")
    public ResponseEntity<PostDto> getPostDetails(@PathVariable Long postId) {
        PostDto postDetails = postService.getPostDetails(postId);
        return ResponseEntity.ok(postDetails);
    }

    // 이미지 업로드
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("upload") MultipartFile file) {
        String imageUrl = fileService.saveImage(file);
        return ResponseEntity.ok(Map.of("default", imageUrl));
    }

    // 게시글 작성하기
    @PostMapping("/write")
    public ResponseEntity<?> writePost(@RequestBody PostDto postDto) {
        postService.writePost(postDto);
        return ResponseEntity.ok(Map.of(
                "message", "게시글이 작성되었습니다.",
                "postId", postDto.getPostId()
        ));
    }

    // 에디터에 업로드했던 임시 이미지 파일 지우기
    @PostMapping("/cleanup-temp")
    public ResponseEntity<?> cleanupTempImages(@RequestBody List<String> unusedImages) {
        fileService.cleanupTempImages(unusedImages);
        return ResponseEntity.ok(Map.of("message", "사용하지 않은 이미지가 삭제되었습니다."));
    }

    // 게시글 수정하기
    @PutMapping("/update/{postId}")
    public ResponseEntity<?> updatePost(@RequestBody PostDto postDto) {
        postService.updatePost(postDto);
        return ResponseEntity.ok(Map.of("message", "게시글이 업데이트되었습니다."));
    }

    // 게시글 삭제하기
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, @RequestParam String deleterId, @RequestParam String postTitle) {
        postService.deletePost(postId, deleterId, postTitle);
        return ResponseEntity.ok(Map.of("message", "게시글이 삭제되었습니다."));
    }

    // 게시글 카테고리(태그) 조회하기
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags () {
        List<String> tags = postService.getAllTags();
        return ResponseEntity.ok(tags);
    }
}
