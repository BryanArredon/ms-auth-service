package com.security.auth_service.repository;

import com.security.auth_service.entity.Aplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AplicacionRepository extends JpaRepository<Aplicacion, UUID> {
    Optional<Aplicacion> findByNombre(String nombre);
}
