package com.renee328.admin.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Locale;

import static com.renee328.admin.util.FilterConstants.*;

public class BlockMaliciousRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BlockMaliciousRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String path = request.getRequestURI();
        final String method = request.getMethod();
        final String userAgent = request.getHeader("User-Agent");
        final String ip = request.getRemoteAddr();
        final String uri = path.toLowerCase(Locale.ROOT);

        // CORS 프리플라이트는 통과
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 의심스러운 JS 요청 차단
        if (SUSPICIOUS_JS_REGEX != null && SUSPICIOUS_JS_REGEX.matcher(uri).matches()) {
            log.warn("[의심 JS 요청] ip={}, uri={}", ip, path);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 의심스러운 정적 리소스 요청.");
            return;
        }

        // 의심스러운 파일 확장자 요청 차단
        if (BLOCKED_EXTENSIONS.stream().anyMatch(uri::endsWith)) {
            log.warn("[의심스러운 확장자 요청] ip={}, uri={}", ip, path);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 의심스러운 파일 요청.");
            return;
        }

        // /api/** 가 아니면 검사 스킵 (SPA 라우트/정적 리소스는 통과)
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 차단 대상 URI 패턴 포함
        if (BLOCKED_URI_PATTERNS.stream().anyMatch(uri::contains)) {
            log.warn("[차단 URI 패턴] ip={}, uri={}", ip, path);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 허용되지 않는 URI 경로.");
            return;
        }

        // User-Agent 체크
        if (REQUIRE_USER_AGENT && (userAgent == null || userAgent.trim().isEmpty())) {
            log.warn("[UA 누락] ip={}, uri={}", ip, path);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: User-Agent 누락.");
            return;
        }
        if (userAgent != null) {
            final String ua = userAgent.toLowerCase(Locale.ROOT);
            final String uaNorm = ua.replaceAll("[^a-z0-9]", "");
            if (BLOCKED_USER_AGENTS.stream().anyMatch(bad ->
                    ua.contains(bad) || uaNorm.contains(bad))) {
                log.warn("[차단 UA] ip={}, ua={}, uri={}", ip, userAgent, path);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 비정상적인 User-Agent.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}