package com.renee328.admin.service;

import com.renee328.dto.AdminDto;
import com.renee328.dto.LoginRequest;
import com.renee328.mapper.AdminMapper;
import com.renee328.mapper.AuthMapper;
import com.renee328.service.AdminService;
import com.renee328.admin.security.SecurityUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

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
    public void updateMyPassword(Long userId, AdminDto adminDto) {
        adminDto.setLoginPassword(passwordEncoder.encode(adminDto.getLoginPassword()));
        adminMapper.updateMyPassword(userId, adminDto);
    }

    // 관리자 계정 삭제
    @Transactional
    @Override
    public void deleteAdmin(String deletedId, String deleterId, Long userId) {
        AdminDto deleter = adminMapper.findByLoginId(deleterId);
        AdminDto target = adminMapper.findByLoginId(deletedId);

        if (!"ROLE_SUPER_ADMIN".equals(deleter.getRoleId())) {
            throw new AccessDeniedException("최고 관리자만 삭제할 수 있습니다.");
        }

        if (deleterId.equals(deletedId)) {
            throw new IllegalArgumentException("자기 자신은 삭제할 수 없습니다.");
        }

        int deleterPriority = adminMapper.getRolePriority(deleter.getRoleId());
        int targetPriority = adminMapper.getRolePriority(target.getRoleId());

        if (deleterPriority <= targetPriority) {
            throw new AccessDeniedException("동일하거나 높은 권한의 관리자는 삭제할 수 없습니다.");
        }
        adminMapper.logAdminDeletion(deletedId, deleterId);
        adminMapper.deleteAdmin(userId);
    }

    // 비밀번호 인증
    @Override
    public Map<String, Boolean> verifyPassword(LoginRequest passwordRequest) {

        if (passwordRequest.getUsername() == null || passwordRequest.getUsername().isEmpty()) {
            throw new IllegalArgumentException("로그인 아이디가 제공되지 않았습니다.");
        }

        String loginId = passwordRequest.getUsername();
        AdminDto adminDto = authMapper.getByUsername(loginId);

        boolean isValidPassword = adminDto != null && passwordEncoder.matches(passwordRequest.getPassword(), adminDto.getLoginPassword());
        return Map.of("success", isValidPassword);
    }

}
