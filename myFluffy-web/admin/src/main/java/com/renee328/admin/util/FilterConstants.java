package com.renee328.admin.util;

import java.util.List;

public class FilterConstants {

    // 차단 대상 URI 패턴
    public static final List<String> BLOCKED_URI_PATTERNS = List.of(
            "/.git", ".env", "/php", "/HNAP1", "/shell", "/wp-", "/actuator", "/cgi-bin", "/p/u", "/doAuthentication"
    );

    // 차단 대상 User-Agent (공격 봇/스크래퍼 등)
    public static final List<String> BLOCKED_USER_AGENTS = List.of(
            "python", "python-requests", "pythonrequests", "curl", "httpclient",
            "go-http-client", "wget", "bot", "scanner", "nmap", "censys",
            "internet-measurement", "internetmeasurement", "censysinspect", "zgrab", "masscan"
    );

    // 정확히 일치하는 단일 허용 경로
    public static final List<String> ALLOWED_EXACT_PATHS = List.of(
            "/", "/index.html", "/favicon.png", "/error"
    );

    // 허용된 접두사 경로 (이걸로 startsWith 검증)
    public static final List<String> ALLOWED_PREFIXES = List.of(
            "/api", "/assets", "/ckeditor5-custom", "/uploads"
    );
}
