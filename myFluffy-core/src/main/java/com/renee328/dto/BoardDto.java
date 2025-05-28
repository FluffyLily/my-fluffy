package com.renee328.dto;

import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;
@ToString
public class BoardDto {
    private Long boardId; // 게시판_ID
    private String boardName; // 게시판_이름
    private LocalDateTime createdAt; // 등록_일시
    private Long createdBy; // 등록자_순번
    private String createdByLoginId; // 등록자_아이디
    private LocalDateTime updatedAt; // 수정_일시
    private Long updatedBy; // 수정자_순번
    private String updatedByLoginId; // 수정자_아이디
    private List<BbsCategoryDto> categories; // 카테고리 아이디, 이름
    private Long boardCategoryId; // 카테고리_아이디
    private String boardCategoryName; // 카테고리_이름
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


    public String getCreatedByLoginId() {
        return createdByLoginId;
    }

    public void setCreatedByLoginId(String createdByLoginId) {
        this.createdByLoginId = createdByLoginId;
    }

    public String getUpdatedByLoginId() {
        return updatedByLoginId;
    }

    public void setUpdatedByLoginId(String updatedByLoginId) {
        this.updatedByLoginId = updatedByLoginId;
    }

    public List<BbsCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<BbsCategoryDto> categories) {
        this.categories = categories;
    }

    public Long getBoardCategoryId() {
        return boardCategoryId;
    }

    public void setBoardCategoryId(Long boardCategoryId) {
        this.boardCategoryId = boardCategoryId;
    }

    public String getBoardCategoryName() {
        return boardCategoryName;
    }

    public void setBoardCategoryName(String boardCategoryName) {
        this.boardCategoryName = boardCategoryName;
    }
}
