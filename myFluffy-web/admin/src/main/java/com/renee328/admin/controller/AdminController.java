package com.renee328.admin.controller;

import com.renee328.dto.AdminDto;
import com.renee328.dto.LoginRequest;
import com.renee328.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import static com.renee328.admin.util.Constants.ADMIN_API_URL;

@RestController
@RequestMapping(ADMIN_API_URL)
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 아이디 중복 체크 API
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String loginId) {
        boolean available = adminService.isUsernameAvailable(loginId);
        return ResponseEntity.ok(available);
    }

    // 관리자 계정 생성
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody AdminDto adminDto) throws AccessDeniedException {
        adminService.createAdmin(adminDto);
        return ResponseEntity.ok(Map.of("message", "관리자 계정이 생성되었습니다."));
    }

    // 관리자 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<AdminDto>> getAdminList() {
        List<AdminDto> adminList = adminService.getAdminList();
        return ResponseEntity.ok(adminList);
    }

    // 관리자 수 조회
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getAdminCount() {
        int totalCount = adminService.getAdminCount();
        Map<String, Object> response = Map.of("totalCount", totalCount);
        return ResponseEntity.ok(response);
    }

    // 관리자 계정 수정
    @PutMapping("/update/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateAdmin(@PathVariable Long userId, @RequestBody AdminDto adminDto) throws AccessDeniedException {
        adminService.updateAdmin(userId, adminDto);
        return ResponseEntity.ok(Map.of("message", "관리자 계정이 업데이트되었습니다."));
    }

    // 관리자 본인 계정 비밀번호 변경
    @PutMapping("/update-profile/{userId}")
    public ResponseEntity<?> updateMyPassword(@PathVariable Long userId, @RequestBody AdminDto adminDto) throws AccessDeniedException {
        adminService.updateMyPassword(userId, adminDto);
        return ResponseEntity.ok(Map.of("message", "관리자 계정의 비밀번호가 업데이트되었습니다."));
    }

    // 비밀번호 인증 절차
    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Boolean>> verifyPassword(@RequestBody LoginRequest passwordRequest) {
        if (passwordRequest.getUsername() == null || passwordRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }

        Map<String, Boolean> response = adminService.verifyPassword(passwordRequest);
        return ResponseEntity.ok(response);
    }

    // 관리자 계정 삭제
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long userId,
                                         @RequestParam String deleterId,
                                         @RequestParam String deletedId) throws AccessDeniedException {
        adminService.deleteAdmin(deletedId, deleterId, userId);
        return ResponseEntity.ok(Map.of("message", "관리자 계정이 삭제되었습니다."));
    }

}