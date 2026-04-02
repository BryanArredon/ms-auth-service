package com.security.auth_service.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String token;
}