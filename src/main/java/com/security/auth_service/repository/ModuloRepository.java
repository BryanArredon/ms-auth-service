package com.security.auth_service.repository;

import com.security.auth_service.entity.ModuloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuloRepository extends JpaRepository<ModuloEntity, Long> {
}
