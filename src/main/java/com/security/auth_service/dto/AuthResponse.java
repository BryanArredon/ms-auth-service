package com.security.auth_service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UUID userId;
    private String correo;
    private String accessToken;
    private String refreshToken;
    private String mensaje;
    private Boolean requiresMfa;
    private UUID tempUserId;
}
