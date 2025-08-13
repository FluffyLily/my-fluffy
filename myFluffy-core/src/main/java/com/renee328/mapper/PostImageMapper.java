package com.renee328.mapper;

import com.renee328.dto.PostImageDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PostImageMapper {

    // 이미지 URL 추가
    void insertPostImage(PostImageDto postImageDto);

    // 게시글에 속한 모든 이미지 조회
    List<String> getImageUrlsByPostId(Long postId);

    // 게시글의 모든 이미지 삭제
    void deletePostImagesByPostId(Long postId);
}
