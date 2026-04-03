package com.security.auth_service.repository;

import com.security.auth_service.entity.PasswordResetTokenEntity;
import com.security.auth_service.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    Optional<PasswordResetTokenEntity> findByToken(String token);
    void deleteByUsuario(UsuarioEntity usuario);
}
