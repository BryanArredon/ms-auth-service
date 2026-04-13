package com.security.auth_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.auth_service.entity.AplicacionEntity;
import com.security.auth_service.entity.AuditoriaEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.AplicacionRepository;
import com.security.auth_service.repository.AuditoriaRepository;
import com.security.auth_service.repository.UsuarioRepository;
import com.security.auth_service.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AplicacionRepository aplicacionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAccion(String accion, String tabla, String datoId, Object anterior, Object nuevo) {
        String currentPrincipal = getCurrentUserCorreo();
        UsuarioEntity usuario = null;
        if (currentPrincipal != null && !currentPrincipal.equals("anonymousUser")) {
            usuario = usuarioRepository.findByCorreo(currentPrincipal).orElse(null);
        }
        
        saveAudit(usuario, null, accion, tabla, datoId, anterior, nuevo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAccion(UUID usuarioId, UUID aplicacionId, String accion, String tabla, String datoId, Object anterior, Object nuevo) {
        UsuarioEntity usuario = usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null;
        AplicacionEntity aplicacion = aplicacionId != null ? aplicacionRepository.findById(aplicacionId).orElse(null) : null;
        
        saveAudit(usuario, aplicacion, accion, tabla, datoId, anterior, nuevo);
    }

    private void saveAudit(UsuarioEntity usuario, AplicacionEntity aplicacion, String accion, String tabla, String datoId, Object anterior, Object nuevo) {
        try {
            String valAnterior = anterior != null ? objectMapper.writeValueAsString(anterior) : null;
            String valNuevo = nuevo != null ? objectMapper.writeValueAsString(nuevo) : null;

            AuditoriaEntity auditoria = AuditoriaEntity.builder()
                    .usuario(usuario)
                    .aplicacion(aplicacion)
                    .accion(accion)
                    .tablaAfectada(tabla)
                    .datoId(datoId)
                    .valorAnterior(valAnterior)
                    .valorNuevo(valNuevo)
                    .build();

            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    private String getCurrentUserCorreo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
    @Override
    public List<AuditoriaEntity> listarTodas() {
        List<AuditoriaEntity> logs = auditoriaRepository.findAll();
        logs.sort(Comparator.comparing(AuditoriaEntity::getFechaAccion).reversed());
        return logs;
    }
}
