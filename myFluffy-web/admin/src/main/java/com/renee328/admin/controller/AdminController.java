package com.renee328.admin.controller;

import com.renee328.dto.AdminDto;
import com.renee328.dto.LoginRequest;
import com.renee328.dto.PasswordChangeRequest;
import com.renee328.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import static com.renee328.admin.util.ApiConstants.ADMIN_API_URL;

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
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long userId, @RequestBody AdminDto adminDto) throws AccessDeniedException {
        adminService.updateAdmin(userId, adminDto);
        return ResponseEntity.ok(Map.of("message", "관리자 계정이 업데이트되었습니다."));
    }

    // 관리자 본인 계정 비밀번호 변경
    @PutMapping("/profile/password")
    public ResponseEntity<?> changeMyPassword(@RequestBody PasswordChangeRequest passwordRequest,
                                              Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (passwordRequest == null || passwordRequest.getCurrentPassword() == null || passwordRequest.getCurrentPassword().isBlank()
                || passwordRequest.getNewPassword() == null || passwordRequest.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호와 새 비밀번호를 모두 입력하세요.");
        }

        String loginId = authentication.getName();
        adminService.changeMyPassword(loginId, passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
    }

    // 관리자 계정 삭제
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long userId,
                                         @RequestBody LoginRequest passwordRequest) throws AccessDeniedException {

        if (passwordRequest == null || passwordRequest.getPassword() == null || passwordRequest.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호를 입력하세요.");
        }

        String password = passwordRequest.getPassword();
        adminService.deleteAdmin(userId, password);
        return ResponseEntity.ok(Map.of("message", "관리자 계정이 삭제되었습니다."));
    }
}