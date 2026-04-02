package com.security.auth_service.service;

import com.security.auth_service.dto.AtualizarCredencialesRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.EliminarCuentaRequest;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse verifyMfa(com.security.auth_service.dto.VerifyMfaRequest request);
    
    com.security.auth_service.dto.MfaSetupResponse setupTotp(String correo);
    
    AuthResponse enableTotp(com.security.auth_service.dto.EnableTotpRequest request);

    AuthResponse block(String correo);

    AuthResponse updateCredentials(AtualizarCredencialesRequest request);

    AuthResponse deleteAccount(EliminarCuentaRequest request);

    AuthResponse logout(LoginRequest request);

    AuthResponse unblock(String correo);
}
