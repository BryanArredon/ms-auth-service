package com.security.auth_service.service.impl;

import com.security.auth_service.dto.ModuloDTO;
import com.security.auth_service.entity.ModuloEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.UsuarioRepository;
import com.security.auth_service.service.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuloServiceImpl implements ModuloService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<ModuloDTO> getModulosPorUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Recolectar módulos de todos los roles del usuario, filtrando activos y sin duplicados
        Set<ModuloEntity> modulos = usuario.getRoles().stream()
                .flatMap(role -> role.getModulos().stream())
                .filter(modulo -> modulo.getActivo() != null && modulo.getActivo())
                .collect(Collectors.toSet());

        return modulos.stream()
                .sorted(Comparator.comparingInt(m -> m.getOrden() != null ? m.getOrden() : 0))
                .map(m -> ModuloDTO.builder()
                        .nombre(m.getNombre())
                        .ruta(m.getRuta())
                        .icono(m.getIcono())
                        .orden(m.getOrden())
                        .build())
                .collect(Collectors.toList());
    }
}
