package com.security.auth_service.service;

import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
