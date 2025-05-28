package com.renee328.mapper;

import com.renee328.dto.PostDto;
import com.renee328.dto.PostSearchCondition;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper {

    // 게시글 목록 조회하기(필터, 검색 조건 적용)
    List<PostDto> getPostList(PostSearchCondition searchCondition);

    // 게시글 수 조회
    int getPostCount(PostSearchCondition searchCondition);

    // 오늘 작성된 게시글 수 조회
    int getTodayPostCount();

    // 최근 7일 간의 게시글 수 조회
    List<Map<String, Object>> getPostCountLast7Days();

    // 게시글 작성하기
    void insertPost(PostDto postDto);

    // 게시글 세부내용 조회
    PostDto getPostDetails(Long postId);

    // 게시글 수정하기
    void updatePost(PostDto postDto);

    // 게시글 삭제하기
    void deletePost(Long postId);

    // 게시글 삭제 로그
    void logPostDeletion(@Param("deleterId") String deleterId, @Param("postTitle") String postTitle);
}
