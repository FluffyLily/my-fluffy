package com.renee328.admin.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = currentUser();
        return (userDetails != null) ? userDetails.getUserId() : null;
    }

    public static String getCurrentLoginId() {
        Authentication authentication = authentication();
        return (authentication != null) ? authentication.getName() : null;
    }

    // 현재 로그인한 사용자의 역할(roleId) 가져오기
    public static String getCurrentUserRole() {
        CustomUserDetails userDetails = currentUser();
        return (userDetails != null) ? userDetails.getRoleId() : null;
    }

    // 특정 역할을 가진 사용자인지 확인
    public static boolean hasRole(String role) {
        CustomUserDetails userDetails = currentUser();
        return userDetails != null && role != null && role.equals(userDetails.getRoleId());
    }

    private static Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) return null;
        return authentication;
    }

    private static CustomUserDetails currentUser() {
        Authentication authentication = authentication();
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        return (principal instanceof CustomUserDetails) ? (CustomUserDetails) principal : null;
    }
}