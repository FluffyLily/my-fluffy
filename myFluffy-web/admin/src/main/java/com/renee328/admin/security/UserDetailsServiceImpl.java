package com.renee328.admin.security;

import com.renee328.mapper.AuthMapper;
import com.renee328.dto.AdminDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthMapper authMapper;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UserDetailsServiceImpl( AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        AdminDto adminDto = authMapper.getByUsername(loginId);

        if (adminDto == null) {
            logger.error("AdminDto is null for username: {}", loginId);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
        }

        return new CustomUserDetails(
                adminDto.getUserId(),
                adminDto.getLoginId(),
                adminDto.getLoginPassword(),
                adminDto.getIsActive(),
                adminDto.getIsInitialized(),
                Collections.singletonList(new SimpleGrantedAuthority(adminDto.getRoleId()))
        );
    }

    @Transactional
    public void updateUserInitializationStatus(Long userId, Boolean isInitialized) {
        authMapper.updateUserInitializationStatus(userId, isInitialized);
    }
}