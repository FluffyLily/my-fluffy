package com.renee328.admin.controller;


import com.renee328.dto.LoginRequest;
import com.renee328.dto.LoginResponse;
import com.renee328.admin.repository.TokenRepository;
import com.renee328.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;

import static com.renee328.admin.util.ApiConstants.AUTH_API_URL;

@RestController
@RequestMapping(AUTH_API_URL)
public class AuthController {

    private final AuthService authService;
    private final TokenRepository tokenRepository;

    private AuthController(AuthService authService, TokenRepository tokenRepository) {
        this.authService = authService;
        this.tokenRepository = tokenRepository;
    }
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.authenticate(loginRequest, response);
            if (loginResponse == null) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "계정이 비활성화되었습니다. 다른 관리자에게 활성화 요청을 해주세요."));
            }

            Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일간 유지

            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(Map.of("accessToken", loginResponse.getAccessToken()));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패: 등록된 사용자가 없습니다.");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "이미 로그아웃된 사용자입니다."));
        }
        String loginId = auth.getName();
        // Redis에서 저장된 액세스 토큰 및 리프레시 토큰 삭제
        tokenRepository.deleteTokens(loginId);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refreshToken 가져오기 (NPE 방지)
        String refreshToken = null;
        if (request.getCookies() != null) {
            refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh Token이 없습니다."));
        }

        try {
            // 새로운 엑세스 토큰과 리프레쉬 토큰 발급
            LoginResponse newTokens = authService.refreshTokens(refreshToken);

            // 새로운 리프레쉬 토큰을 쿠키에 저장
            Cookie refreshTokenCookie = new Cookie("refreshToken", newTokens.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(Map.of("accessToken", newTokens.getAccessToken()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "토큰을 리프레쉬할 수 없습니다."));
        }
    }
}
