package com.security.auth_service.service;

import com.security.auth_service.dto.ModuloDTO;
import java.util.List;

public interface ModuloService {
    List<ModuloDTO> getModulosPorUsuarioActual();
}
