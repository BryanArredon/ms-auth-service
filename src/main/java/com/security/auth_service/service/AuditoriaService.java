package com.security.auth_service.service;

import java.util.List;
import java.util.UUID;
import com.security.auth_service.entity.AuditoriaEntity;

public interface AuditoriaService {
    void registrarAccion(String accion, String tabla, String datoId, Object anterior, Object nuevo);
    void registrarAccion(UUID usuarioId, UUID aplicacionId, String accion, String tabla, String datoId, Object anterior, Object nuevo);
    List<AuditoriaEntity> listarTodas();
}
