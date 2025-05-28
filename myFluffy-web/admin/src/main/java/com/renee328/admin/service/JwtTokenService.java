package com.renee328.admin.service;

import com.renee328.dto.LoginResponse;
import com.renee328.admin.repository.TokenRepository;
import com.renee328.admin.security.UserDetailsServiceImpl;
import com.renee328.admin.security.CustomUserDetails;
import com.renee328.util.JwtManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class JwtTokenService {
    @Value("${security.jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${security.jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;
    private final JwtManager jwtManager;
    private final TokenRepository tokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public JwtTokenService(JwtManager jwtManager, TokenRepository tokenRepository, UserDetailsServiceImpl userDetailsService) {
        this.jwtManager = jwtManager;
        this.tokenRepository = tokenRepository;
        this.userDetailsService = userDetailsService;
    }

    // Access Token 생성
    public String generateAccessToken(String loginId, String role, Long userId) {
        return jwtManager.createToken(loginId, ACCESS_TOKEN_EXPIRATION_TIME, role, userId, null);
    }

    // Refresh Token 생성
    public String generateRefreshToken(String loginId) {
        return jwtManager.createToken(loginId, REFRESH_TOKEN_EXPIRATION_TIME, null, null, null);
    }

    // 새로운 Access Token 발급
    @Transactional
    public LoginResponse refreshTokens(String refreshToken) {

        // 1. 리프레쉬 토큰 검증
        if (!validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레쉬 토큰");
        }

        // 2. 리프레쉬 토큰에서 사용자 정보 추출
        String loginId =jwtManager.extractLoginId(refreshToken);
        String roleId = getRoleIdForUser(loginId);
        Long userId = getUserIdForUser(loginId);

        // 3. Redis에서 저장된 리프레쉬 토큰과 비교
        String storedRefreshToken = tokenRepository.getRefreshToken(loginId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 일치하지 않음");
        }

        // 4. 새로운 Access Token 발급
        String newAccessToken = generateAccessToken(loginId, roleId, userId);
        tokenRepository.saveAccessToken(loginId, newAccessToken, ACCESS_TOKEN_EXPIRATION_TIME);

        // 5. 리프레쉬 토큰 갱신
        String newRefreshToken = generateRefreshToken(loginId);
        tokenRepository.saveRefreshToken(loginId, newRefreshToken, REFRESH_TOKEN_EXPIRATION_TIME);

        return new LoginResponse(newAccessToken,newRefreshToken);
    }

    public String getRoleIdForUser(String loginId) {
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(loginId);
        if (userDetails != null) {
            return userDetails.getRoleId();
        }
        return null;
    }

    public Long getUserIdForUser(String loginId) {
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(loginId);
        if (userDetails != null) {
            return userDetails.getUserId();
        }
        return null;
    }

    // JWT 검증
    public String extractLoginId(String token) {
        return jwtManager.getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        return jwtManager.validateToken(token);
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        String role = extractRoleFromToken(token);  // 토큰에서 권한 정보를 추출하는 메서드 구현 필요
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
    public String extractRoleFromToken(String token) {
        try {
            // JWT 토큰에서 클레임 정보를 가져옴
            String role = jwtManager.getClaims(token).get("role", String.class);

            if (role == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "권한 정보가 포함되지 않은 토큰");
            }

            return role;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "권한 정보 추출 중 오류 발생");
        }
    }

}
