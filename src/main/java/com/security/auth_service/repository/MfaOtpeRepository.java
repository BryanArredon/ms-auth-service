package com.security.auth_service.repository;

import com.security.auth_service.entity.MfaOtpeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MfaOtpeRepository extends JpaRepository<MfaOtpeEntity, UUID> {
    Optional<MfaOtpeEntity> findByCodigo(String codigo);
    Optional<MfaOtpeEntity> findByUsuarioId(UUID usuarioId);
    
    void deleteByExpiraEnBefore(java.time.LocalDateTime date);
}
