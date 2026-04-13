package com.security.auth_service.service;

import com.security.auth_service.dto.AssignRoleRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.CrearRolRequest;
import com.security.auth_service.entity.RolEntity;

import java.util.List;

public interface AdminRoleService {
    RolEntity crearRol(CrearRolRequest request);
    List<RolEntity> listarRoles();
    AuthResponse asignarRol(AssignRoleRequest request);
    AuthResponse removerRol(AssignRoleRequest request);
}
