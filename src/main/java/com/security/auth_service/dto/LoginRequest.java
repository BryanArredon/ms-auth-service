package com.security.auth_service.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String correo;
    private String password;
}
