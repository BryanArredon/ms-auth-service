package com.security.auth_service.service;

import com.security.auth_service.entity.UsuarioEntity;
import java.util.UUID;

public interface SessionHistoryService {
    void registrarSesion(UsuarioEntity usuario, UUID aplicacionId, String ip, String userAgent);
}
