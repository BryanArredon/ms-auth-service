package com.security.auth_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String correo;
    private String password;
}
