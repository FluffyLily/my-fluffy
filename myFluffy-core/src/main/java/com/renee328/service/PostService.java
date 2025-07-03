package com.renee328.service;

import com.renee328.dto.PostDto;
import com.renee328.dto.PostSearchCondition;
import java.util.List;
import java.util.Map;

public interface PostService {
    // 게시글 전체 목록 조회하기
    List<PostDto> getPostList(PostSearchCondition searchCondition);

    // 게시글 작성하기
    void writePost(PostDto postDto);

    // 전체 게시글 수 조회하기
    int getPostCount(PostSearchCondition searchCondition);

    // 오늘 작성된 게시글 수 조회하기
    int getTodayPostCount();

    List<Map<String, Object>> getPostCountLast7Days();

    // 게시글 세부내용 조회하기
    PostDto getPostDetails(Long postId);

    // 게시글 수정하기
    void updatePost(PostDto postDto);

    // 게시글 내용만 조회하기 (삭제 전 이미지 정리용)
    String getPostContentById(Long postId);

    // 게시물 삭제하기
    void deletePost(Long postId, String deleterId, String postTitle);

    // 게시글 카테고리(태그) 조회하기
    List<String> getAllTags();
}
