package com.security.auth_service.service;

import java.util.UUID;

import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.entity.UsuarioEntity;

public interface  JwtRefreshService {

    String generateRefreshToken(UsuarioEntity usuario);

    boolean validateRefreshToken(String token);

    String getUsernameFromRefreshToken(String token);

    String rotateRefreshToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);

    void revokeAllUserTokens(UUID userId);

    AuthResponse refreshAccessToken(String refreshToken);
    
}
