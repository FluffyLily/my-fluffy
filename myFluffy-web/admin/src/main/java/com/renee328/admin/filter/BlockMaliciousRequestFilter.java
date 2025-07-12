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
import java.util.List;

public class BlockMaliciousRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BlockMaliciousRequestFilter.class);
    private static final List<String> blockedUriPatterns = List.of(
            "/.git", ".env", "/php", "/HNAP1", "/shell", "/wp-", "/actuator", "/cgi-bin"
    );

    private static final List<String> blockedUserAgents = List.of(
            "python", "python-requests", "pythonrequests", "curl", "httpclient", "go-http-client", "wget", "bot",
            "scanner", "nmap", "censys", "internet-measurement", "internetmeasurement", "censysinspect", "zgrab", "masscan"
    );

    private static final List<String> allowedPrefixes = List.of(
            "/", "/api", "/assets", "/ckeditor5-custom", "/uploads"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();

        // 차단할 URI 포함 여부
        for (String pattern : blockedUriPatterns) {
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

        // 허용된 경로 접두사만 통과
        boolean allowed = allowedPrefixes.stream().anyMatch(uri::startsWith);
        if (!allowed) {
            log.warn("[허용되지 않은 접두사로 시작하는 경로로 접근] IP: {}, URI: {}", ip, uri);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 허용되지 않은 접두사로 시작하는 경로로 접근함.");
            return;
        }

        // 비정상 User-Agent 차단
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            for (String badUserAgent : blockedUserAgents) {
                if (ua.contains(badUserAgent)) {
                    log.warn("[비정상적인 User-Agent 접근] IP: {}, UA: {}, URI: {}", ip, userAgent, uri);
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden: 비정상적인 User-Agent 접근함.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}