package com.security.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.auth_service.dto.AtualizarCredencialesRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.EliminarCuentaRequest;
import com.security.auth_service.dto.EnableTotpRequest;
import com.security.auth_service.dto.ForgotPasswordRequest;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.LogoutRequest;
import com.security.auth_service.dto.MfaSetupResponse;
import com.security.auth_service.dto.RefreshTokenRequest;
import com.security.auth_service.dto.RegisterRequest;
import com.security.auth_service.dto.ResetPasswordRequest;
import com.security.auth_service.dto.ValidateResetTokenRequest;
import com.security.auth_service.dto.VerifyMfaRequest;
import com.security.auth_service.service.AuthService;
import com.security.auth_service.service.JwtRefreshService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtRefreshService jwtRefreshService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(@RequestBody VerifyMfaRequest request) {
        return ResponseEntity.ok(authService.verifyMfa(request));
    }

    @GetMapping("/mfa/setup-totp/{correo}")
    public ResponseEntity<MfaSetupResponse> setupTotp(@PathVariable String correo) {
        return ResponseEntity.ok(authService.setupTotp(correo));
    }

    @PostMapping("/mfa/enable-totp")
    public ResponseEntity<AuthResponse> enableTotp(@RequestBody EnableTotpRequest request) {
        return ResponseEntity.ok(authService.enableTotp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(jwtRefreshService.refreshAccessToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PutMapping("/bloquear-cuenta/{correo}")
    public ResponseEntity<AuthResponse> block(@PathVariable String correo) {
        return ResponseEntity.ok(authService.block(correo));
    }

    @PutMapping("/desbloquear-cuenta/{correo}")
    public ResponseEntity<AuthResponse> unblock(@PathVariable String correo) {
        return ResponseEntity.ok(authService.unblock(correo));
    }

    @PutMapping("/actualizar-credenciales")
    public ResponseEntity<AuthResponse> updateCredentials(@RequestBody AtualizarCredencialesRequest request) {
        return ResponseEntity.ok(authService.updateCredentials(request));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<AuthResponse> validateResetToken(@RequestBody ValidateResetTokenRequest request) {
        return ResponseEntity.ok(authService.validateResetToken(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
