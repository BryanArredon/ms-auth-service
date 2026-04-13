package com.security.auth_service.repository;

import com.security.auth_service.entity.HistorialSesionesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialSesionesRepository extends JpaRepository<HistorialSesionesEntity, Long> {
    List<HistorialSesionesEntity> findByUsuarioId(UUID usuarioId);
}
