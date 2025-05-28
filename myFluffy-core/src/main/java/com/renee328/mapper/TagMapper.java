package com.renee328.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {
    // 게시글 카테고리 (태그) 생성하기
    void insertTagIfNotExists(String tagName);
    // 게시글 카테고리 (태그) 목록 조회하기
    List<String> findAllTagNames();
    // 게시글 카테고리 (태그) 아이디 조회
    Integer findTagIdByName(@Param("tagName") String tagName);
}
