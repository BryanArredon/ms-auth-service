package com.security.auth_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.security.auth_service.entity.JwtRefreshEntity;

@Repository
public interface JwtRefreshRepository extends JpaRepository<JwtRefreshEntity, UUID> {
    
    Optional<JwtRefreshEntity> findByToken(String token);
    
    List<JwtRefreshEntity> findAllByUsuarioIdAndRevocadoFalse(UUID usuarioId);
}
