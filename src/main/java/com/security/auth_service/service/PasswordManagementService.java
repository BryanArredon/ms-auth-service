package com.security.auth_service.service;

import com.security.auth_service.entity.PasswordHistoryEntity;
import com.security.auth_service.entity.PasswordResetTokenEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.PasswordHistoryRepository;
import com.security.auth_service.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordManagementService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int PASSWORD_HISTORY_LIMIT = 5; // No permitir reutilizar últimas 5 contraseñas
    private static final int PASSWORD_EXPIRY_DAYS = 90;

    /**
     * Guarda la contraseña anterior en historial
     */
    @Transactional
    public void savePasswordToHistory(UsuarioEntity usuario, String passwordHash) {
        PasswordHistoryEntity history = PasswordHistoryEntity.builder()
                .usuario(usuario)
                .passwordHash(passwordHash)
                .build();
        passwordHistoryRepository.save(history);

        // Limpiar historial si excede límite
        List<PasswordHistoryEntity> histories = passwordHistoryRepository
                .findByUsuarioOrderByFechaCambioDesc(usuario);
        if (histories.size() > PASSWORD_HISTORY_LIMIT) {
            passwordHistoryRepository.deleteAll(histories.subList(PASSWORD_HISTORY_LIMIT, histories.size()));
        }
    }

    /**
     * Validar si la nueva contraseña NO fue usada en el historial
     */
    public boolean isPasswordInHistory(UsuarioEntity usuario, String newPassword) {
        List<PasswordHistoryEntity> histories = passwordHistoryRepository
                .findByUsuarioOrderByFechaCambioDesc(usuario);

        return histories.stream()
                .anyMatch(h -> passwordEncoder.matches(newPassword, h.getPasswordHash()));
    }

    /**
     * Marcar contraseña como expirada (vence en 90 días)
     */
    public void setPasswordExpiry(UsuarioEntity usuario) {
        usuario.setPasswordExpiradoEn(LocalDateTime.now().plusDays(PASSWORD_EXPIRY_DAYS));
    }

    /**
     * Verificar si la contraseña del usuario ha expirado
     */
    public boolean isPasswordExpired(UsuarioEntity usuario) {
        if (usuario.getPasswordExpiradoEn() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(usuario.getPasswordExpiradoEn());
    }

    /**
     * Generar token de recuperación de contraseña
     */
    @Transactional
    public String generatePasswordResetToken(UsuarioEntity usuario) {
        // Invalidar tokens anteriores
        passwordResetTokenRepository.deleteByUsuario(usuario);

        String token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                .usuario(usuario)
                .token(token)
                .estirado(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        return token;
    }

    /**
     * Validar token de recuperación (no expirado y no usado)
     */
    public PasswordResetTokenEntity validateResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(t -> !t.getEstirado())
                .filter(t -> LocalDateTime.now().isBefore(t.getFechaExpiracion()))
                .orElse(null);
    }

    /**
     * Marcar token como usado
     */
    @Transactional
    public void invalidateResetToken(PasswordResetTokenEntity token) {
        token.setEstirado(true);
        passwordResetTokenRepository.save(token);
    }

    /**
     * Obtener días hasta vencimiento de contraseña
     */
    public long getDaysUntilPasswordExpiry(UsuarioEntity usuario) {
        if (usuario.getPasswordExpiradoEn() == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                usuario.getPasswordExpiradoEn()
        );
    }
}
