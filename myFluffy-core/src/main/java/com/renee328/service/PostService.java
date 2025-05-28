package com.renee328.service;

import com.renee328.dto.PostDto;
import com.renee328.dto.PostSearchCondition;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface PostService {
    // 게시글 전체 목록 조회하기
    @Transactional
    List<PostDto> getPostList(PostSearchCondition searchCondition);

    // 게시글 작성하기
    @Transactional
    void writePost(PostDto postDto);

    // 전체 게시글 수 조회하기
    @Transactional
    int getPostCount(PostSearchCondition searchCondition);

    // 오늘 작성된 게시글 수 조회하기
    @Transactional
    int getTodayPostCount();

    @Transactional
    List<Map<String, Object>> getPostCountLast7Days();

    // 게시글 세부내용 조회하기
    @Transactional
    PostDto getPostDetails(Long postId);

    // 게시글 수정하기
    @Transactional
    void updatePost(PostDto postDto);

    // 게시물 삭제하기
    @Transactional
    void deletePost(Long postId, String deleterId, String postTitle);

    // 게시글 카테고리(태그) 조회하기
    @Transactional
    List<String> getAllTags();
}
