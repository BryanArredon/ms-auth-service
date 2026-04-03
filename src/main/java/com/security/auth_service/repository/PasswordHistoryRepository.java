package com.security.auth_service.repository;

import com.security.auth_service.entity.PasswordHistoryEntity;
import com.security.auth_service.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryEntity, UUID> {
    List<PasswordHistoryEntity> findByUsuarioOrderByFechaCambioDesc(UsuarioEntity usuario);
}
