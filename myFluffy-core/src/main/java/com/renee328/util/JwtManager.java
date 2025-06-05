package com.renee328.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
@Component
public class JwtManager {
    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;

    public String createToken(String loginId, long expirationTime, String role, Long userId, String userName) {
        return Jwts.builder()
                .subject(loginId)
                .claim("role", role)
                .claim("userId", userId)
                .claim("name", userName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println(">> JWT 만료됨: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println(">> JWT 유효성 검증 실패: " + e.getMessage());
            return false;
        }
    }


    public String extractLoginId(String token) {
        return getClaims(token).getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
}
