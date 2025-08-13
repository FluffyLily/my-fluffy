package com.renee328.admin.filter;

import com.renee328.admin.service.JwtTokenService;
import com.renee328.admin.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");
        String path = request.getRequestURI();

        log.debug("접속 로그 - IP: {}, Agent: {}, Path: {}", ip, agent, path);

        if (path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/assets/")
                || path.startsWith("/uploads/")
                || path.equals("/index.html")) {
            chain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims = jwtTokenService.parseClaimsOrNull(token);
        if (claims == null) {
            chain.doFilter(request, response);
            return;
        }

        String loginId = claims.getSubject();
        String roleId  = claims.get("role", String.class);
        Long userId = null;
        Object userIdByToken = claims.get("userId");
        if (userIdByToken instanceof Number) userId = ((Number) userIdByToken).longValue();
        else if (userIdByToken instanceof String) userId = Long.valueOf((String) userIdByToken);

        if (!StringUtils.hasText(loginId) || !StringUtils.hasText(roleId) || userId == null) {

            chain.doFilter(request, response);
            return;
        }

        var authorities = List.of(new SimpleGrantedAuthority(roleId));
        var principal = new CustomUserDetails(
                userId,
                loginId,
                "",
                true,    // isActive
                true,           // isInitialized
                authorities
        );

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}

