package com.security.auth_service.repository;

import com.security.auth_service.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
    Optional<UsuarioEntity> findByCorreo(String correo);
    Optional<UsuarioEntity> findById(Long id);
    Optional<UsuarioEntity> findByCorreoAndCuentaBloqueadaFalse(String correo);
}
