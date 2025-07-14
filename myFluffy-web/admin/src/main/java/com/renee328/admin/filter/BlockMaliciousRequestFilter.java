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
import static com.renee328.admin.util.FilterConstants.*;

public class BlockMaliciousRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BlockMaliciousRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();

        // 차단할 URI 포함 여부
        for (String pattern : BLOCKED_URI_PATTERNS) {
            if (uri.toLowerCase().contains(pattern)) {
                log.warn("[허용되지 않는 URI 패턴 경로로 접근] IP: {}, URI: {}, 패턴: {}", ip, uri, pattern);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 허용되지 않는 URI 패턴 경로로 접근함.");
                return;
            }
        }

        // admin으로 시작하는 경로 차단 (단, /api/admin/**는 제외)
        if ((uri.equals("/admin") || uri.startsWith("/admin/")) && !uri.startsWith("/api/admin")) {
            log.warn("[허용되지 않는 관리자(/admin) 엔드포인트로 접근] IP: {}, URI: {}", ip, uri);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 허용되지 않는 관리자(/admin) 엔드포인트로 접근함.");
            return;
        }

        // login으로 시작하는 경로 차단 (단, /api/auth/login은 제외)
        if (!uri.startsWith("/api/auth") && (uri.equals("/login") || uri.startsWith("/login/"))) {
            log.warn("[허용되지 않는 로그인(/login) 엔드포인트로 접근] IP: {}, URI: {}", ip, uri);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 허용되지 않는 로그인(/login) 엔드포인트로 접근함.");
            return;
        }

        // 단일 경로 허용
        if (ALLOWED_EXACT_PATHS.contains(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 허용된 경로 접두사만 통과
        boolean allowed = ALLOWED_PREFIXES.stream().anyMatch(uri::startsWith);
        if (!allowed) {
            log.warn("[허용되지 않은 접두사로 시작하는 경로로 접근] IP: {}, URI: {}", ip, uri);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 허용되지 않은 접두사로 시작하는 경로로 접근함.");
            return;
        }

        // 의심스러운 파일 확장자 접근 차단
        if (uri.endsWith(".php") || uri.endsWith(".env") || uri.endsWith(".bak")) {
            log.warn("[의심스러운 파일 확장자 접근] IP: {}, URI: {}", ip, uri);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 의심스러운 파일 요청.");
            return;
        }

        // 의심스러운 JS 리소스 요청 차단
        if (uri.matches("^/(jquery|bootstrap|config|env|main).*\\.js$")) {
            log.warn("[의심스러운 JS 리소스 요청 탐지] IP: {}, URI: {}", ip, uri);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 존재하지 않는 정적 리소스 요청");
            return;
        }

        // 비정상 User-Agent 차단
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();

            // 전역 User-Agent 필터링
            for (String badUserAgent : BLOCKED_USER_AGENTS) {
                if (ua.contains(badUserAgent)) {
                    log.warn("[비정상적인 User-Agent 접근] IP: {}, UA: {}, URI: {}", ip, userAgent, uri);
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 비정상적인 User-Agent.");
                    return;
                }
            }

            // 정적 리소스 접근 시 UA 추가 필터링
            if ((uri.startsWith("/assets") || uri.startsWith("/ckeditor5-custom"))) {
                for (String badUserAgent : BLOCKED_USER_AGENTS) {
                    if (ua.contains(badUserAgent)) {
                        log.warn("[정적 리소스에 비정상 UA 접근 차단] IP: {}, UA: {}, URI: {}", ip, userAgent, uri);
                        response.setStatus(HttpStatus.NOT_ACCEPTABLE.value()); // 406
                        response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 비정상 UA로 정적 리소스 접근함.");
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}