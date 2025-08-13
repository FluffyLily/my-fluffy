package com.renee328.service;

import com.renee328.dto.AdminDto;
import java.util.List;

public interface AdminService {
    // 아이디 중복 확인
    boolean isUsernameAvailable(String loginId);

    // 관리자 계정 생성
    void createAdmin(AdminDto adminDto);

    // 관리자 목록 조회
    List<AdminDto> getAdminList();

    // 관리자 수 조회
    int getAdminCount();

    // 관리자 계정 수정
    void updateAdmin(Long userId, AdminDto adminDto);

    // 관리자 본인 계정 비밀번호 변경
    void changeMyPassword(String loginId, String currentPassword, String newPassword);

    // 관리자 계정 삭제
    void deleteAdmin(Long targetUserId, String rawPassword);
}
