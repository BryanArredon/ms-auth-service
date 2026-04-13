package com.security.auth_service.service.impl;

import com.security.auth_service.dto.AssignRoleRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.CrearRolRequest;
import com.security.auth_service.entity.AplicacionEntity;
import com.security.auth_service.entity.RolEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.AplicacionRepository;
import com.security.auth_service.repository.RolRepository;
import com.security.auth_service.repository.UsuarioRepository;
import com.security.auth_service.service.AdminRoleService;
import com.security.auth_service.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl implements AdminRoleService {

    private final RolRepository rolRepository;
    private final AplicacionRepository aplicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public RolEntity crearRol(CrearRolRequest request) {
        String baseRole = request.getNombreRol().replaceFirst("^ROLE_", "").toUpperCase();

        Optional<RolEntity> existente = rolRepository.findByNombreRol(baseRole);
        if (existente.isPresent()) {
            throw new RuntimeException("El rol ya existe");
        }

        AplicacionEntity aplicacion = null;
        if (request.getAplicacionId() != null) {
            aplicacion = aplicacionRepository.findById(request.getAplicacionId())
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
        }

        RolEntity nuevoRol = RolEntity.builder()
                .nombreRol(baseRole)
                .aplicacion(aplicacion)
                .build();

        RolEntity guardado = rolRepository.save(nuevoRol);
        
        auditoriaService.registrarAccion("CREATE", "roles", guardado.getId().toString(), null, guardado);
        
        return guardado;
    }

    @Override
    public List<RolEntity> listarRoles() {
        return rolRepository.findAll();
    }

    @Override
    @Transactional
    public AuthResponse asignarRol(AssignRoleRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int rolesNuevos = 0;
        for (String roleStr : request.getRoles()) {
            String roleName = roleStr.replaceFirst("^ROLE_", "").toUpperCase();
            RolEntity rol = rolRepository.findByNombreRol(roleName)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
            if (!usuario.getRoles().contains(rol)) {
                usuario.getRoles().add(rol);
                rolesNuevos++;
            }
        }

        if (rolesNuevos > 0) {
            UsuarioEntity salvo = usuarioRepository.save(usuario);
            auditoriaService.registrarAccion("ASSIGN_ROLES", "usuarios", salvo.getId().toString(), null, salvo.getRoles());
        }

        return AuthResponse.builder()
                .correo(usuario.getCorreo())
                .userId(usuario.getId())
                .mensaje("Asignación de roles completada exitosamente.")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse removerRol(AssignRoleRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int rolesRemovidos = 0;
        for (String roleStr : request.getRoles()) {
            String roleName = roleStr.replaceFirst("^ROLE_", "").toUpperCase();
            RolEntity rol = rolRepository.findByNombreRol(roleName)
                    .orElse(null);
            
            if (rol != null && usuario.getRoles().contains(rol)) {
                usuario.getRoles().remove(rol);
                rolesRemovidos++;
            }
        }

        if (rolesRemovidos > 0) {
            UsuarioEntity salvo = usuarioRepository.save(usuario);
            auditoriaService.registrarAccion("REMOVE_ROLES", "usuarios", salvo.getId().toString(), null, salvo.getRoles());
        }

        return AuthResponse.builder()
                .correo(usuario.getCorreo())
                .userId(usuario.getId())
                .mensaje("Remoción de roles completada exitosamente.")
                .build();
    }
}
