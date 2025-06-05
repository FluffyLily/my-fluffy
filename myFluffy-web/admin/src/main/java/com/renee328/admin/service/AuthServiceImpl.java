package com.renee328.admin.service;

import com.renee328.dto.LoginRequest;
import com.renee328.dto.LoginResponse;
import com.renee328.admin.repository.TokenRepository;
import com.renee328.service.AuthService;
import com.renee328.admin.security.UserDetailsServiceImpl;
import com.renee328.admin.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {
    @Value("${security.jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${security.jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;
    private final JwtTokenService jwtTokenService;
    private final TokenRepository tokenRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);


    public AuthServiceImpl(JwtTokenService jwtTokenService, TokenRepository tokenRepository, UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRepository = tokenRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    @Override
    public LoginResponse authenticate(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            // 1. 입력한 로그인 아이디 검증
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(loginRequest.getUsername());

            // 2. Spring Security의 `isEnabled()`가 false인 경우 직접 403 응답 반환
            if (!userDetails.isEnabled()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "계정이 비활성화되었습니다.");
                return null;
            }

            // 3. 비밀번호 검증 (BCryptPasswordEncoder 암호화 알고리즘)
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword());
            if (!passwordMatches) {
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }

            // 4. 로그인 시 초기화 상태 변경 (새로운 CustomUserDetails 객체 생성)
            CustomUserDetails updatedUserDetails = new CustomUserDetails(
                    userDetails.getUserId(),
                    userDetails.getUsername(),
                    userDetails.getPassword(),
                    userDetails.isEnabled(),
                    true,  // isInitialized 설정
                    userDetails.getAuthorities()
            );
            userDetailsService.updateUserInitializationStatus(userDetails.getUserId(), true);  // DB에 업데이트

            // 5. SecurityContext 인증 정보 저장
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails, null, updatedUserDetails.getAuthorities()
            );
//            authentication.setDetails(updatedUserDetails.getUserId());
            SecurityContextHolder.getContext().setAuthentication(authentication);


            // 6. 인증된 사용자 정보로 JWT 토큰 생성
            String roleId = updatedUserDetails.getRoleId();
            String loginId = updatedUserDetails.getUsername();
            Long userId = updatedUserDetails.getUserId();
            String accessToken = jwtTokenService.generateAccessToken(loginId, roleId, userId);
            String refreshToken = jwtTokenService.generateRefreshToken(loginId);

            // 7. Redis JWT 토큰 저장
            tokenRepository.saveAccessToken(loginId, accessToken, ACCESS_TOKEN_EXPIRATION_TIME);
            tokenRepository.saveRefreshToken(loginId, refreshToken, REFRESH_TOKEN_EXPIRATION_TIME);

            // 8. 생성된 JWT 토큰을 컨트롤러에 응답
            return new LoginResponse(accessToken, refreshToken);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("인증에 실패했습니다 : " + e.getMessage());
        }
    }

    // 리프레시 토큰을 사용해 새로운 토큰 발급
    public LoginResponse refreshTokens(String refreshToken) {
        return jwtTokenService.refreshTokens(refreshToken);
    }


}
