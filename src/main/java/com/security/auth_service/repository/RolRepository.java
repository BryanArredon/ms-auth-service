package com.security.auth_service.repository;

import com.security.auth_service.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRolAndAplicacionId(String nombreRol, UUID aplicacionId);
}
