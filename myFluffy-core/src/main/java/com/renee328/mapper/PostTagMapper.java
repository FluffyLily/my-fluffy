package com.renee328.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostTagMapper {
    void insertPostTag(@Param("postId") Long postId, @Param("tagId") int tagId);

    void deleteByPostId(@Param("postId") Long postId);
}
