package com.renee328.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class AdminDto {

    private Long userId;             // 사용자_순번
    private String roleId;           // 역할_ID
    private String roleName;         // 역할_이름
    private Integer priority;        // 역할_우선순위
    private LocalDateTime createdAt; // 등록_일시
    private Long createdBy;          // 등록자_순번
    private LocalDateTime updatedAt; // 수정_일시
    private Long updatedBy;          // 수정자_순번
    @JsonProperty("isInitialized")
    private Boolean isInitialized;    // 초기화_가부
    private Boolean isActive;         // 사용_가부
    private String loginToken;       // 로그인_토큰
    private String loginPassword;    // 로그인_비밀번호
    private String loginId;          // 로그인_아이디

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    @JsonProperty("isInitialized")
    public Boolean getIsInitialized() {
        return isInitialized;
    }

    public void setIsInitialized(Boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    @JsonProperty("isActive")
    public Boolean getIsActive() { return isActive; }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
}
