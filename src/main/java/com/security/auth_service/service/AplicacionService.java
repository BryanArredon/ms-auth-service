
package com.security.auth_service.service;

import com.security.auth_service.dto.CrearAplicacionRequest;
import com.security.auth_service.entity.AplicacionEntity;
import java.util.List;
import java.util.UUID;

public interface AplicacionService {
    List<AplicacionEntity> listarTodas();
    AplicacionEntity obtenerPorId(UUID id);
    AplicacionEntity crear(CrearAplicacionRequest request);
    AplicacionEntity actualizar(UUID id, AplicacionEntity aplicacion);
    void eliminar(UUID id);
}
