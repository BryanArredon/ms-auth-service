package com.security.auth_service.repository;

import com.security.auth_service.entity.AplicacionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AplicacionRepository extends JpaRepository<AplicacionEntity, UUID> {
    Optional<AplicacionEntity> findByNombre(String nombre);
}
