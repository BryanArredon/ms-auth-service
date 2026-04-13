package com.security.auth_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CrearRolRequest {
    private String nombreRol;
    private UUID aplicacionId; // Opcional si solo es 1 app
}
