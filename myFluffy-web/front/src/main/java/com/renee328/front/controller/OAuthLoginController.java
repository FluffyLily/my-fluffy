package com.renee328.front.controller;

import com.renee328.util.JwtManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.renee328.front.util.Constants.OAUTH_API_URL;

@RestController
@RequestMapping(OAUTH_API_URL)
public class OAuthLoginController {

    private final JwtManager jwtManager;
    private final String redirectUri;

    @Value("${app.is-local}")
    private boolean isLocal;

    public OAuthLoginController(JwtManager jwtManager,
                                @Value("${app.oauth.redirect-uri}") String redirectUri) {
        this.jwtManager = jwtManager;
        this.redirectUri = redirectUri;
    }

    @GetMapping("/success")
    public void oauth2Success(HttpServletResponse response, Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        String accessToken = jwtManager.createToken(email, 3600 * 1000L, "ROLE_USER", null, name);

        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(!isLocal)
                .sameSite(isLocal ? "Lax" : "None")
                .path("/")
                .maxAge(3600)
                .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());

        // HTML 리디렉트 응답
        String html = String.format("""
            <html>
              <head><meta charset="UTF-8"></head>
              <body>
                <script>
                  setTimeout(function () {
                    window.location.href = '%s/oauth-redirect';
                  }, 200);
                </script>
              </body>
            </html>
            """, redirectUri);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> result = new HashMap<>();

        if (authentication.getPrincipal() instanceof Map principalMap) {
            result.put("loginId", principalMap.get("loginId"));
            result.put("userName", principalMap.get("userName"));
        } else if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            result.put("loginId", oAuth2User.getAttribute("email"));
            result.put("userName", oAuth2User.getAttribute("name"));
        } else {
            result.put("loginId", authentication.getPrincipal());
            result.put("userName", "사용자"); // fallback
        }

        result.put("userType", "USER");
        return ResponseEntity.ok(result);
    }
}
