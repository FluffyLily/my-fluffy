package com.renee328.admin.filter;

import com.renee328.admin.service.JwtTokenService;
import com.renee328.admin.security.UserDetailsServiceImpl;
import com.renee328.admin.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");
        String path = request.getRequestURI();

        log.info("접속 로그 - IP: {}, Agent: {}, Path: {}", ip, agent, path);

        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/refresh")
                || path.startsWith("/assets/") || path.startsWith("/uploads/") || path.equals("/index.html")) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isValid = jwtTokenService.validateToken(token);
        if (!isValid) {
            chain.doFilter(request, response);
            return;
        }

        String loginId = jwtTokenService.extractLoginId(token);
        Long userId = jwtTokenService.getUserIdForUser(loginId);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(loginId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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

