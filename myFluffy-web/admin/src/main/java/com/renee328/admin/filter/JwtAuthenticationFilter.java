package com.renee328.admin.filter;

import com.renee328.admin.service.JwtTokenService;
import com.renee328.admin.security.UserDetailsServiceImpl;
import com.renee328.admin.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/refresh")
                || path.startsWith("/assets/") || path.startsWith("/uploads/") || path.equals("/index.html")) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        // 토큰이 없을 경우 필터 진행
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
        System.out.println("JwtAuthenticationFilter [bearerToken] : " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}

