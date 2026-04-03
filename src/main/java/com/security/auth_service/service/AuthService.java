package com.security.auth_service.service;

import com.security.auth_service.dto.AtualizarCredencialesRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.EliminarCuentaRequest;
import com.security.auth_service.dto.ForgotPasswordRequest;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.LogoutRequest;
import com.security.auth_service.dto.RegisterRequest;
import com.security.auth_service.dto.ResetPasswordRequest;
import com.security.auth_service.dto.ValidateResetTokenRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse verifyMfa(com.security.auth_service.dto.VerifyMfaRequest request);

    AuthResponse block(String correo);

    AuthResponse updateCredentials(AtualizarCredencialesRequest request);

    AuthResponse deleteAccount(EliminarCuentaRequest request);

    AuthResponse logout(LogoutRequest request);

    AuthResponse unblock(String correo);

    AuthResponse forgotPassword(ForgotPasswordRequest request);

    AuthResponse validateResetToken(ValidateResetTokenRequest request);

    AuthResponse resetPassword(ResetPasswordRequest request);
}
