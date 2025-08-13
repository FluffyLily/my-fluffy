package com.renee328.admin.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class TokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public TokenRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Access Token 저장 (만료 시간 설정)
    public void saveAccessToken(String loginId, String accessToken, long accessTokenExpirationTime) {
        String key = "ACCESS_TOKEN:" + loginId;
        redisTemplate.opsForValue().set(key, accessToken, accessTokenExpirationTime, TimeUnit.MILLISECONDS);
    }

    // Refresh Token 저장
    public void saveRefreshToken(String loginId, String refreshToken, long refreshTokenExpirationTime) {
        String key = "REFRESH_TOKEN:" + loginId;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpirationTime, TimeUnit.MILLISECONDS);
    }

    // Access Token 조회
    public String getAccessToken(String loginId) {
        String key = "ACCESS_TOKEN:" + loginId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    // Refresh Token 조회
    public String getRefreshToken(String loginId) {
        String key = "REFRESH_TOKEN:" + loginId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    // 토큰 삭제 (로그아웃 시 사용)
    public void deleteTokens(String loginId) {
        redisTemplate.delete("ACCESS_TOKEN:" + loginId);
        redisTemplate.delete("REFRESH_TOKEN:" + loginId);
    }
}