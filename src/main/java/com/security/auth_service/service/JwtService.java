
package com.security.auth_service.service;

import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.entity.UsuarioEntity;

public interface JwtService {
    String generateToken(UsuarioEntity usuario);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);

    AuthResponse validate(String token);
}
