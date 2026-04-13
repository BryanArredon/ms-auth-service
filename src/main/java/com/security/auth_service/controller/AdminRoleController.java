package com.security.auth_service.controller;

import com.security.auth_service.dto.AssignRoleRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.CrearRolRequest;
import com.security.auth_service.entity.RolEntity;
import com.security.auth_service.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @PostMapping
    public ResponseEntity<RolEntity> crearRol(@RequestBody CrearRolRequest request) {
        return ResponseEntity.ok(adminRoleService.crearRol(request));
    }

    @GetMapping
    public ResponseEntity<List<RolEntity>> listarRoles() {
        return ResponseEntity.ok(adminRoleService.listarRoles());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<AuthResponse> asignarRol(@RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(adminRoleService.asignarRol(request));
    }

    @DeleteMapping("/usuarios")
    public ResponseEntity<AuthResponse> removerRol(@RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(adminRoleService.removerRol(request));
    }
}
