package com.renee328.service.impl;

import com.renee328.dto.AdminDto;
import com.renee328.dto.PostDto;
import com.renee328.dto.PostImageDto;
import com.renee328.dto.PostSearchCondition;
import com.renee328.mapper.*;
import com.renee328.service.FileService;
import com.renee328.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final PostImageMapper postImageMapper;
    private final AuthMapper authMapper;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    public PostServiceImpl(PostMapper postMapper,
                           TagMapper tagMapper,
                           PostTagMapper postTagMapper,
                           PostImageMapper postImageMapper,
                           AuthMapper authMapper,
                           FileService fileService,
                           PasswordEncoder passwordEncoder) {
        this.postMapper = postMapper;
        this.tagMapper = tagMapper;
        this.postTagMapper = postTagMapper;
        this.postImageMapper = postImageMapper;
        this.authMapper = authMapper;
        this.fileService = fileService;
        this.passwordEncoder = passwordEncoder;
    }

    // 게시글 목록 조회하기 (필터, 검색, 정렬 조건 적용)
    @Override
    public List<PostDto> getPostList(PostSearchCondition searchCondition) {
        return postMapper.getPostList(searchCondition);
    }

    // 전체 게시글 수 조회하기 (필터, 검색 조건 적용)
    @Override
    public int getPostCount(PostSearchCondition searchCondition) {
        return postMapper.getPostCount(searchCondition);
    }

    // 오늘 작성된 게시글 수 조회
    @Override
    public int getTodayPostCount() {
        return postMapper.getTodayPostCount();
    }

    // 최근 7일 간의 게시글 수 조회
    @Override
    public List<Map<String, Object>> getPostCountLast7Days() {
        return postMapper.getPostCountLast7Days();
    }

    // 게시글 세부내용 조회
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

    // 게시글 작성하기
    @Transactional
    @Override
    public void writePost(PostDto postDto) {
        setPostCategoryString(postDto);
        postMapper.insertPost(postDto);
        insertTag(postDto);
        Long postId = postDto.getPostId();
        saveImages(postDto, postId);
    }

    // 게시글 수정하기
    @Transactional
    @Override
    public void updatePost(PostDto postDto) {
        setPostCategoryString(postDto);
        postMapper.updatePost(postDto);
        Long postId = postDto.getPostId();
        postTagMapper.deleteByPostId(postId);
        insertTag(postDto);
        cleanupUnusedImages(postDto, postId);
        postImageMapper.deletePostImagesByPostId(postId);
        saveImages(postDto, postId);
    }

    // 게시글 삭제하기
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_MANAGER')")
    @Override
    public void deletePost(Long postId, String deleterLoginId, String rawPassword) {

        AdminDto deleter = authMapper.getByUsername(deleterLoginId);
        if (deleter == null || !passwordEncoder.matches(rawPassword, deleter.getLoginPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        String postTitle = postMapper.getPostTitleById(postId);
        if (postTitle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        // 서버에서 이미지 파일 지우기
        fileService.cleanupUnusedImages(postId, Collections.emptyList());
        postMapper.logPostDeletion(deleterLoginId, postTitle);
        postTagMapper.deleteByPostId(postId);
        postImageMapper.deletePostImagesByPostId(postId);
        postMapper.deletePost(postId);
    }

    // 게시글 카테고리 (태그) 조회하기
    @Override
    public List<String> getAllTags() {
        return tagMapper.findAllTagNames();
    }

    // 게시글 이미지 저장하기
    private void saveImages(PostDto postDto, Long postId) {
        if (postDto.getImages() != null && !postDto.getImages().isEmpty()) {
            for (PostImageDto postImageDto : postDto.getImages()) {
                postImageDto.setPostId(postId);
                postImageMapper.insertPostImage(postImageDto);
            }
        }
    }

    // 게시글에서 더 이상 사용하지 않는 이미지 삭제(수정 시)
    private void cleanupUnusedImages(PostDto postDto, Long postId) {
        List<String> usedImages;
        if (postDto.getImages() != null) {
            usedImages = postDto.getImages().stream()
                    .map(PostImageDto::getImageUrl)
                    .collect(Collectors.toList());
        } else {
            usedImages = List.of();
        }
        fileService.cleanupUnusedImages(postId, usedImages);
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

                    Integer tagId = tagMapper.findTagIdByName(tagName);
                    if (tagId != null) {
                        postTagMapper.insertPostTag(postDto.getPostId(), tagId);
                    }
                }
            }
        }
    }
}
