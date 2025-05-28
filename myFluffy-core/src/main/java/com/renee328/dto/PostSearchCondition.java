package com.renee328.dto;

import com.renee328.enums.SearchType;
import com.renee328.enums.SortType;

public class PostSearchCondition {
    private Long boardId;           // 게시판
    private String searchKeyword;   // 검색 키워드
    private SearchType searchType;  // 검색 타입 (제목+내용, 제목, 내용, 작성자, 태그)
    private Boolean isVisible;      // 노출 여부 (비노출 글 제외 체크)
    private SortType sort;          // 정렬 순서 (최신 순, 오래된 순)
    private Integer offset;
    private Integer limit;

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public SortType getSort() {
        return sort;
    }

    public void setSort(SortType sort) {
        this.sort = sort;
    }

    public int getOffset() {
        return (offset == null || offset < 0) ? 0 : offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return (limit == null || limit <= 0) ? 10 : limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
