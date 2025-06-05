package com.renee328.dto;

import com.renee328.enums.SearchType;
import com.renee328.enums.SortType;

public class UserSearchCondition {
    private String userTypeCode;
    private String searchKeyword;
    private SearchType searchType;
    private SortType sort;
    private Integer offset;
    private Integer limit;

    public String getUserTypeCode() {
        return userTypeCode;
    }

    public void setUserTypeCode(String userTypeCode) {
        this.userTypeCode = userTypeCode;
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
