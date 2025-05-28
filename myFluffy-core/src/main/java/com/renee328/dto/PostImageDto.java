package com.renee328.dto;

public class PostImageDto {
    private Long id;          // 이미지 ID
    private Long postId;      // 이미지가 속한 게시글의 ID
    private String imageUrl;  // 이미지 URL

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}