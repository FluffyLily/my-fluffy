package com.renee328.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {
    private Long postId; // 게시물_순번
    private Long boardId; // 게시판_아이디
    private String boardName; // 게시판_이름
    private Long boardCategoryId; // 게시판_카테고리_아이디
    private String title; // 게시물_제목
    private String content; // 게시물_내용
    private Boolean isVisible; // 노출_가부
    private Integer sortOrder; // 정렬_순서
    private Integer viewCount; // 조회수
    private Boolean isPinned; // 상단_고정_가부
    private Long authorId; // 작성자_순번
    private List<PostImageDto> images; // 이미지_URL
    private List<String> postCategory; // 게시물 태그 실제 목록 (join용)
    private String postCategoryString; // 캐시용 필드 (DB POST_CATEGORY 컬럼)
    private LocalDateTime createdAt; // 등록_일자
    private Long createdBy; // 등록자_순번
    private String createdByName; // 등록자_이름
    private LocalDateTime updatedAt; // 수정_일자
    private Long updatedBy; // 수정자_순번
    private String updatedByName; // 수정자_이름

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public Long getBoardCategoryId() {
        return boardCategoryId;
    }

    public void setBoardCategoryId(Long boardCategoryId) {
        this.boardCategoryId = boardCategoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public List<PostImageDto> getImages() {
        return images;
    }

    public void setImages(List<PostImageDto> images) {
        this.images = images;
    }

    public List<String> getPostCategory() {
        return postCategory;
    }

    public void setPostCategory(List<String> postCategory) {
        this.postCategory = postCategory;
    }

    public String getPostCategoryString() {
        return postCategoryString;
    }

    public void setPostCategoryString(String postCategoryString) {
        this.postCategoryString = postCategoryString;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
}
