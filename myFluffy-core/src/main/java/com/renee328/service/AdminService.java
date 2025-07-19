package com.renee328.service;

import com.renee328.dto.AdminDto;
import com.renee328.dto.LoginRequest;
import java.util.List;
import java.util.Map;

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
    void updateMyPassword(Long userId, AdminDto adminDto);

    // 관리자 계정 삭제
    void deleteAdmin(String deletedId, String deleterId, Long userId);

    // 비밀번호 인증
    Map<String, Boolean> verifyPassword(LoginRequest passwordRequest);

}
