package com.renee328.admin.service;

import com.renee328.admin.security.CustomUserDetails;
import com.renee328.dto.AdminDto;
import com.renee328.mapper.AdminMapper;
import com.renee328.mapper.AuthMapper;
import com.renee328.service.AdminService;
import com.renee328.admin.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    private final AdminMapper adminMapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminMapper adminMapper, AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 아이디 중복 확인
    @Override
    public boolean isUsernameAvailable(String loginId) {
        return adminMapper.countByLoginId(loginId) == 0;
    }

    // 관리자 계정 생성
    @Transactional
    @Override
    public void createAdmin(AdminDto adminDto) {
        adminDto.setLoginPassword(passwordEncoder.encode(adminDto.getLoginPassword()));
        adminMapper.insertAdmin(adminDto);
    }

    // 관리자 목록 조회
    @Override
    public List<AdminDto> getAdminList() {
        return adminMapper.getAdminList();
    }

    // 관리자 수 조회
    @Override
    public int getAdminCount() {
        return adminMapper.getAdminCount();
    }

    // 관리자 계정 수정
    @Transactional
    @Override
    public void updateAdmin(Long userId, AdminDto adminDto) {
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        Long currentUserId = SecurityUtil.getCurrentUserId();
        String currentLoginId = SecurityUtil.getCurrentLoginId();
        AdminDto targetAdmin = adminMapper.findByUserId(userId);
        String targetLoginId = targetAdmin.getLoginId();

        int currentPriority = adminMapper.getRolePriority(currentUserRole);
        int targetPriority = adminMapper.getRolePriority(targetAdmin.getRoleId());

        if (currentUserId.equals(userId)) {
            if (!adminDto.getRoleId().equals(currentUserRole)) {
                throw new AccessDeniedException("자기 자신의 권한은 수정할 수 없습니다.");
            }
        }
        if (currentPriority < targetPriority && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("수정할 권한이 없습니다.");
        }
        if (currentPriority == targetPriority && !currentUserRole.equals(targetAdmin.getRoleId())) {
            throw new AccessDeniedException("동일 권한의 역할은 수정할 수 없습니다.");
        }
        // demo 계정 수정 권한 제한
        if (currentLoginId != null && currentLoginId.startsWith("demo-") &&
                targetLoginId != null && !(targetLoginId.startsWith("demo-") || targetLoginId.startsWith("sample-"))) {
            throw new AccessDeniedException("데모 계정은 다른 데모 또는 샘플 계정만 수정할 수 있습니다.");
        }
        adminMapper.updateAdmin(userId, adminDto);
    }

    // 관리자 본인 계정 비밀번호 변경
    @Transactional
    @Override
    public void changeMyPassword(String loginId, String currentPassword, String newPassword) {
        AdminDto admin = authMapper.getByUsername(loginId);
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(currentPassword, admin.getLoginPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, admin.getLoginPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이전 비밀번호와 동일합니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        AdminDto updatedAdmin = new AdminDto();
        updatedAdmin.setLoginPassword(encodedPassword);
        updatedAdmin.setUpdatedBy(admin.getUserId());

        adminMapper.updateMyPassword(admin.getUserId(), updatedAdmin);
    }

    // 관리자 계정 삭제
    @Transactional
    public void deleteAdmin(Long targetUserId, String rawPassword) {
        // 1. 현재 로그인 사용자
        CustomUserDetails authentication = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String deleterLoginId = authentication.getUsername();

        // 2. 비밀번호 검증
        AdminDto deleter = authMapper.getByUsername(deleterLoginId);
        if (deleter == null || !passwordEncoder.matches(rawPassword, deleter.getLoginPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        // 3. 최고 관리자/본인 계정/권한 레벨 검증
        AdminDto target = adminMapper.findByUserId(targetUserId);
        String deletedLoginId = target.getLoginId();

        if (!"ROLE_SUPER_ADMIN".equals(deleter.getRoleId())) {
            throw new AccessDeniedException("최고 관리자만 삭제할 수 있습니다.");
        }

        if (deleterLoginId.equals(deletedLoginId)) {
            throw new IllegalArgumentException("자기 자신은 삭제할 수 없습니다.");
        }

        int deleterPriority = adminMapper.getRolePriority(deleter.getRoleId());
        int targetPriority = adminMapper.getRolePriority(target.getRoleId());
        if (deleterPriority <= targetPriority) {
            throw new AccessDeniedException("동일하거나 높은 권한의 관리자는 삭제할 수 없습니다.");
        }

        // 4. 삭제
        adminMapper.logAdminDeletion(deletedLoginId, deleterLoginId);
        adminMapper.deleteAdmin(targetUserId);
    }

}
