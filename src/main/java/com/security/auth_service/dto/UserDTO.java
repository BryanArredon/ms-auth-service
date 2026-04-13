package com.security.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String correo;
    private String nombre;
    private String apellidos;
    private String numeroEmpleado;
    private String especialidad;
    private Set<String> roles;
}
