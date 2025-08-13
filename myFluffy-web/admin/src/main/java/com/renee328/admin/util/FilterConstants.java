package com.renee328.admin.util;

import java.util.List;
import java.util.regex.Pattern;

public class FilterConstants {

    // 차단 대상 URI 패턴
    public static final List<String> BLOCKED_URI_PATTERNS = List.of(
            "/.git", ".env", "/php", "/HNAP1", "/shell", "/wp-", "/actuator", "/cgi-bin",
            "/p/u", "/doAuthentication", "/.well-known", "/boaform", "/setup.cgi",
            "/admin/config", "/debug", "/config", "/passwd" );

    // 의심스러운 파일 확장자
    public static final List<String> BLOCKED_EXTENSIONS = List.of(".php", ".env", ".bak");

    // 차단 대상 User-Agent (공격 봇/스크래퍼 등)
    public static final List<String> BLOCKED_USER_AGENTS = List.of(
            // 자동화 도구/스크립트
            "curl", "wget", "python", "pythonrequests", "httpclient", "gohttpclient",
            "nodefetch", "axios", "javahttp", "libwww", "urllib", "perl", "lwp-request",

            // 크롤러/봇
            "bot", "crawler", "spider", "scrapy", "crawl", "harvest",

            // 스캐너
            "scanner", "nmap", "masscan", "zgrab", "zgrab2", "censys", "censysinspect",

            // 측정기/익스플로잇 플랫폼
            "internetmeasurement", "internet-measurement", "expanse", "xpanse"
    );

    // User-Agent 필수 여부
    public static final boolean REQUIRE_USER_AGENT = true;

    // 의심스러운 JS 요청 패턴 (필요 없으면 null 가능)
    public static final Pattern SUSPICIOUS_JS_REGEX =
            Pattern.compile("^/(jquery|bootstrap|config|env|main).*\\.js$", Pattern.CASE_INSENSITIVE);

}
