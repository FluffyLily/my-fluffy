package com.renee328.service;

import com.renee328.dto.LoginRequest;
import com.renee328.dto.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponse authenticate(LoginRequest loginRequest, HttpServletResponse response);
    LoginResponse refreshTokens(String refreshToken);

}
