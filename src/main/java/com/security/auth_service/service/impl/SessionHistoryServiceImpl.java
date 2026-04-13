package com.security.auth_service.service.impl;

import com.security.auth_service.entity.AplicacionEntity;
import com.security.auth_service.entity.HistorialSesionesEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.AplicacionRepository;
import com.security.auth_service.repository.HistorialSesionesRepository;
import com.security.auth_service.service.SessionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionHistoryServiceImpl implements SessionHistoryService {

    private final HistorialSesionesRepository historialSesionesRepository;
    private final AplicacionRepository aplicacionRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSesion(UsuarioEntity usuario, UUID aplicacionId, String ip, String userAgent) {
        AplicacionEntity aplicacion = null;
        if (aplicacionId != null) {
            aplicacion = aplicacionRepository.findById(aplicacionId).orElse(null);
        }

        HistorialSesionesEntity session = HistorialSesionesEntity.builder()
                .usuario(usuario)
                .aplicacion(aplicacion)
                .fechaInicio(LocalDateTime.now())
                .ipOrigen(ip)
                .userAgent(userAgent)
                .estado("ACTIVA")
                .build();

        historialSesionesRepository.save(session);
    }
}
