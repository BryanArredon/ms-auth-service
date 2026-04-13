package com.security.auth_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignRoleRequest {
    private String correo;
    private List<String> roles;
}
