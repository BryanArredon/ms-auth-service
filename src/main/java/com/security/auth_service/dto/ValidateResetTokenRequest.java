package com.security.auth_service.dto;

import lombok.Data;

@Data
public class ValidateResetTokenRequest {
    private String token;
}
