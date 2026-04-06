package com.security.auth_service.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.entity.JwtRefreshEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.JwtRefreshRepository;
import com.security.auth_service.service.JwtRefreshService;
import com.security.auth_service.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtRefreshImpl implements JwtRefreshService {

    private final JwtRefreshRepository jwtRefreshRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public String generateRefreshToken(UsuarioEntity usuario) {
        // Generate a secure random token
        String token = UUID.randomUUID().toString();

        // Set expiration to 7 days from now
        LocalDateTime expiration = LocalDateTime.now().plusDays(7);

        JwtRefreshEntity refreshEntity = JwtRefreshEntity.builder()
                .usuario(usuario)
                .token(token)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(expiration)
                .revocado(false)
                .build();

        jwtRefreshRepository.save(refreshEntity);

        return token;
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return jwtRefreshRepository.findByToken(token)
                .map(entity -> !entity.getRevocado() && entity.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    public String getUsernameFromRefreshToken(String token) {
        return jwtRefreshRepository.findByToken(token)
                .map(entity -> entity.getUsuario().getCorreo())
                .orElse(null);
    }

    @Override
    @Transactional
    public String rotateRefreshToken(String oldToken) {
        // Revoke the old token
        revokeRefreshToken(oldToken);

        // Get the user from the old token
        UsuarioEntity usuario = jwtRefreshRepository.findByToken(oldToken)
                .map(JwtRefreshEntity::getUsuario)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Generate new token
        return generateRefreshToken(usuario);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        jwtRefreshRepository.findByToken(token)
                .ifPresent(entity -> {
                    entity.setRevocado(true);
                    jwtRefreshRepository.save(entity);
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        List<JwtRefreshEntity> tokens = jwtRefreshRepository.findAllByUsuarioIdAndRevocadoFalse(userId);
        
        tokens.forEach(entity -> {
            entity.setRevocado(true);
            jwtRefreshRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String username = getUsernameFromRefreshToken(refreshToken);
        if (username == null) {
            throw new RuntimeException("User not found for refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateToken(username);

        // Optionally rotate refresh token for security
        String newRefreshToken = rotateRefreshToken(refreshToken);

        return AuthResponse.builder()
                .correo(username)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .mensaje("Tokens refreshed successfully")
                .build();
    }
}
