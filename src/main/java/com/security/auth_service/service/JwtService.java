package com.security.auth_service.service;

import com.security.auth_service.dto.AuthResponse;

public interface JwtService {
    String generateToken(String username);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);

    AuthResponse validate(String token);
}
