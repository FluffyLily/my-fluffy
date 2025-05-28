package com.renee328.service.impl;

import com.renee328.dto.PostDto;
import com.renee328.dto.PostImageDto;
import com.renee328.dto.PostSearchCondition;
import com.renee328.dto.TagDto;
import com.renee328.mapper.PostImageMapper;
import com.renee328.mapper.PostMapper;
import com.renee328.mapper.PostTagMapper;
import com.renee328.mapper.TagMapper;
import jdk.javadoc.doclet.Taglet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements com.renee328.service.PostService {
    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final PostImageMapper postImageMapper;

    public PostServiceImpl(PostMapper postMapper, TagMapper tagMapper, PostTagMapper postTagMapper, PostImageMapper postImageMapper) {
        this.postMapper = postMapper;
        this.tagMapper = tagMapper;
        this.postTagMapper = postTagMapper;
        this.postImageMapper = postImageMapper;
    }

    // 게시글 목록 조회하기 (필터, 검색, 정렬 조건 적용)
    @Transactional
    @Override
    public List<PostDto> getPostList(PostSearchCondition searchCondition) {
        return postMapper.getPostList(searchCondition);
    }

    // 전체 게시글 수 조회하기 (필터, 검색 조건 적용)
    @Transactional
    @Override
    public int getPostCount(PostSearchCondition searchCondition) {
        return postMapper.getPostCount(searchCondition);
    }

    // 오늘 작성된 게시글 수 조회
    @Transactional
    @Override
    public int getTodayPostCount() {
        return postMapper.getTodayPostCount();
    }

    // 최근 7일 간의 게시글 수 조회
    @Transactional
    @Override
    public List<Map<String, Object>> getPostCountLast7Days() {
        return postMapper.getPostCountLast7Days();
    }

    // 게시글 작성하기
    @Transactional
    @Override
    public void writePost(PostDto postDto) {
        setPostCategoryString(postDto);
        postMapper.insertPost(postDto);
        insertTag(postDto);

        // 이미지 저장
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            for (PostImageDto postImageDto : postDto.getImages()) {
                postImageDto.setPostId(postDto.getPostId());
                postImageMapper.insertPostImage(postImageDto);  // 이미지 저장
            }
        }
    }

    // 게시글 세부내용 조회하기
    @Transactional
    @Override
    public PostDto getPostDetails(Long postId) {
        PostDto postDto = postMapper.getPostDetails(postId);

        // 이미지 URL 리스트 추가
        List<String> imageUrls = postImageMapper.getImageUrlsByPostId(postId);
        postDto.setImages(imageUrls.stream()
                .map(url -> {
                    PostImageDto postImageDto = new PostImageDto();
                    postImageDto.setImageUrl(url);
                    return postImageDto;
                })
                .toList());

        return postDto;
    }

    // 게시글 수정하기
    @Transactional
    @Override
    public void updatePost(PostDto postDto) {
        setPostCategoryString(postDto);
        postMapper.updatePost(postDto);
        postTagMapper.deleteByPostId(postDto.getPostId());
        postImageMapper.deletePostImagesByPostId(postDto.getPostId());
        insertTag(postDto);

        // 새로운 이미지 저장
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            for (PostImageDto postImageDto : postDto.getImages()) {
                postImageDto.setPostId(postDto.getPostId());  // 게시글과 이미지 연결
                postImageMapper.insertPostImage(postImageDto);  // 새 이미지 저장
            }
        }
    }

    // 게시글 삭제하기
    @Transactional
    @Override
    public void deletePost(Long postId, String deleterId, String postTitle) {
        postMapper.logPostDeletion(deleterId, postTitle);
        postTagMapper.deleteByPostId(postId);
        postImageMapper.deletePostImagesByPostId(postId);
        postMapper.deletePost(postId);
    }

    // 게시글 카테고리 (태그) 조회하기
    @Transactional
    @Override
    public List<String> getAllTags() {
        return tagMapper.findAllTagNames();
    }

    // 캐시용 게시글 카테고리 (태그) 저장하기
    private static void setPostCategoryString(PostDto postDto) {
        postDto.setPostCategoryString(
                postDto.getPostCategory() != null
                        ? String.join(",", postDto.getPostCategory())
                        : null
        );
    }

    // DB에 게시글 카테고리 (태그) 저장하기
    private void insertTag(PostDto postDto) {
        List<String> tagList = postDto.getPostCategory();
        if (tagList != null && !tagList.isEmpty()) {
            for (String tag : tagList) {
                if (tag != null && !tag.isBlank()) {
                    String tagName = tag.trim().replace("#", "");
                    tagMapper.insertTagIfNotExists(tagName);

                    // 태그 이름으로 tagId 조회
                    Integer tagId = tagMapper.findTagIdByName(tagName);
                    if (tagId != null) {
                        postTagMapper.insertPostTag(postDto.getPostId(), tagId);
                    }
                }
            }
        }
    }

}
