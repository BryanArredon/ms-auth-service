package com.security.auth_service.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.security.auth_service.dto.AtualizarCredencialesRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.EliminarCuentaRequest;
import com.security.auth_service.dto.EnableTotpRequest;
import com.security.auth_service.dto.ForgotPasswordRequest;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.LogoutRequest;
import com.security.auth_service.dto.MfaSetupResponse;
import com.security.auth_service.dto.RegisterRequest;
import com.security.auth_service.dto.ResetPasswordRequest;
import com.security.auth_service.dto.ValidateResetTokenRequest;
import com.security.auth_service.dto.VerifyMfaRequest;
import com.security.auth_service.entity.MfaOtpeEntity;
import com.security.auth_service.entity.PasswordResetTokenEntity;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.MfaOtpeRepository;
import com.security.auth_service.repository.UsuarioRepository;
import com.security.auth_service.service.AuthService;
import com.security.auth_service.service.EmailService;
import com.security.auth_service.service.JwtRefreshService;
import com.security.auth_service.service.JwtService;
import com.security.auth_service.service.PasswordManagementService;
import com.security.auth_service.service.TokenBlacklistService;
import com.security.auth_service.service.TotpService;
import com.security.auth_service.utils.OtpGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceimpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final MfaOtpeRepository mfaOtpeRepository;
    private final EmailService emailService;
    private final OtpGenerator otpGenerator;
    private final PasswordManagementService passwordManagementService;
    private final TotpService totpService;
    private final JwtRefreshService jwtRefreshService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        usuarioRepository.findByCorreo(request.getCorreo())
                .ifPresent(usuario -> 
                    { throw new RuntimeException("El correo ya está registrado"); });
        UsuarioEntity nuevoUsuario = UsuarioEntity.builder() 
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        
        // Establecer expiración de contraseña (90 días desde creación)
        passwordManagementService.setPasswordExpiry(nuevoUsuario);
        
        usuarioRepository.save(nuevoUsuario);

        return buildAuthResponse(nuevoUsuario, "Registro exitoso");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UsuarioEntity user = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (Boolean.TRUE.equals(user.getCuentaBloqueada())) {
            throw new RuntimeException("Cuenta bloqueada. Por favor restablece tu acceso.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            registrarIntentoFallido(user.getId()); // Solo pasar el ID
            throw new RuntimeException("Credenciales inválidas");
        }

        // Contraseña correcta: resetear contador
        resetearIntentosFallidos(user.getId());

        user = validarEstadoUsuario(user);

        // Generar OTP
        String otp = otpGenerator.generateOtp();
        
        // Reutilizar el registro si existe, o crear uno nuevo (evita error de constraint única)
        MfaOtpeEntity mfaEntity = mfaOtpeRepository.findByUsuarioId(user.getId())
                .orElse(new MfaOtpeEntity());
                
        mfaEntity.setUsuario(user);
        mfaEntity.setCodigo(otp);
        mfaEntity.setFechaCreacion(LocalDateTime.now());
        mfaEntity.setExpiraEn(LocalDateTime.now().plusMinutes(5));
        
        mfaOtpeRepository.save(mfaEntity);
        
        // Enviar correo
        emailService.sendOtpEmail(user.getCorreo(), otp);
        
        return AuthResponse.builder()
                .requiresMfa(true)
                .tempUserId(user.getId())
                .mensaje("MFA requerido. Hemos enviado un código a tu correo.")
                .build();
    }

    @Transactional
    @Override
    public AuthResponse verifyMfa(VerifyMfaRequest request) {
        UsuarioEntity user = null;
        boolean isValid = false;

        // Opción 1: Validar contra correo (OTP Temporal)
        java.util.Optional<MfaOtpeEntity> mfaEntityOpt = mfaOtpeRepository.findByUsuarioId(request.getTempUserId());
        if (mfaEntityOpt.isPresent() && mfaEntityOpt.get().getExpiraEn().isAfter(LocalDateTime.now())) {
            MfaOtpeEntity mfaEntity = mfaEntityOpt.get();
            if (mfaEntity.getCodigo().equals(request.getOtp())) {
                isValid = true;
                user = mfaEntity.getUsuario();
                mfaOtpeRepository.delete(mfaEntity); 
            }
        }

        // Opción 2: Validar contra Google Authenticator (TOTP)
        if (!isValid) {
            user = usuarioRepository.findById(request.getTempUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado basado en tempUserId"));
            if (Boolean.TRUE.equals(user.getGoogleAuthActivo())) {
                if (totpService.verifyCode(user.getGoogleAuthSecret(), request.getOtp())) {
                    isValid = true;
                }
            }
        }

        if (!isValid) {
            throw new RuntimeException("Código OTP o TOTP incorrecto/expirado");
        }

        // user está asignado aquí
        user.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(user);

        return buildAuthResponse(user, "Login exitoso");
    }

    @Transactional
    @Override
    public MfaSetupResponse setupTotp(String correo) {
        UsuarioEntity user = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        String secret = totpService.generateSecret();
        user.setGoogleAuthSecret(secret);
        user.setGoogleAuthActivo(false); 
        usuarioRepository.save(user);
        
        String qrUri = totpService.getUriForImage(secret, user.getCorreo());
        return MfaSetupResponse.builder().secret(secret).qrDataUri(qrUri).build();
    }

    @Transactional
    @Override
    public AuthResponse enableTotp(EnableTotpRequest request) {
        UsuarioEntity user = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        if (user.getGoogleAuthSecret() == null) {
            throw new RuntimeException("No has iniciado la configuración TOTP");
        }
        
        if (!totpService.verifyCode(user.getGoogleAuthSecret(), request.getCode())) {
            throw new RuntimeException("Código de Google Autenticator inválido");
        }
        
        user.setGoogleAuthActivo(true);
        usuarioRepository.save(user);
        
        return buildAuthResponse(user, "Google Authenticator activado con éxito");
    }
    
    /**
     * Registra un intento fallido de login EN SU PROPIA TRANSACCIÓN COMPLETA
     * Usa el ID para evitar problemas de detached entities
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoFallido(UUID userId) {
        UsuarioEntity usuarioActual = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        int fallidos = java.util.Objects.requireNonNullElse(usuarioActual.getIntentosFallidos(), 0) + 1;
        usuarioActual.setIntentosFallidos(fallidos);
        
        System.out.println("Estado ANTES: intentos_fallidos = " + (usuarioActual.getIntentosFallidos() - 1) + 
                          " | cuenta_bloqueada = " + usuarioActual.getCuentaBloqueada());
        
        if (fallidos >= 5) {
            usuarioActual.setCuentaBloqueada(true);
            System.out.println("CUENTA BLOQUEADA: " + usuarioActual.getCorreo() + " después de " + fallidos + " intentos");
        } else {
            System.out.println("Intento fallido #" + fallidos + " para: " + usuarioActual.getCorreo());
        }
        
        UsuarioEntity guardado = usuarioRepository.saveAndFlush(usuarioActual);
        System.out.println("GUARDADO en BD: intentos_fallidos = " + guardado.getIntentosFallidos() + 
                          " | cuenta_bloqueada = " + guardado.getCuentaBloqueada());
    }

    /**
     * Resetea los intentos fallidos cuando el login es exitoso
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetearIntentosFallidos(UUID userId) {
        UsuarioEntity usuarioActual = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuarioActual.setIntentosFallidos(0);
        usuarioActual.setCuentaBloqueada(false);
        usuarioRepository.saveAndFlush(usuarioActual);
        
        System.out.println("Intentos reseteados para: " + usuarioActual.getCorreo());
    }
    
    private UsuarioEntity validarEstadoUsuario(UsuarioEntity usuario) {     
        if (Boolean.TRUE.equals(usuario.getCuentaBloqueada())) {
            throw new RuntimeException("Su cuenta está bloqueada");
        }

        // Validar si la contraseña ha expirado
        if (passwordManagementService.isPasswordExpired(usuario)) {
            long diasVencidos = Math.abs(passwordManagementService.getDaysUntilPasswordExpiry(usuario));
            throw new RuntimeException("Tu contraseña ha expirado hace " + diasVencidos + " días. Por favor, cámbiala.");
        }

        return usuario;
    }

    private AuthResponse buildAuthResponse(UsuarioEntity usuario, String mensaje) {
        String token = jwtService.generateToken(usuario.getCorreo());
        String refreshToken = jwtRefreshService.generateRefreshToken(usuario);
        return AuthResponse.builder()
                .userId(usuario.getId())
                .correo(usuario.getCorreo())
                .accessToken(token)
                .refreshToken(refreshToken)
                .mensaje(mensaje)
                .build();
    }

    @Override
    public AuthResponse block(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setCuentaBloqueada(true);
        usuarioRepository.save(usuario);
        
        return buildAuthResponse(usuario, "Cuenta bloqueada exitosamente");
    }

    @Override
    public AuthResponse unblock(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setCuentaBloqueada(false);
        usuarioRepository.save(usuario);
        
        return buildAuthResponse(usuario, "Cuenta desbloqueada exitosamente");
    }

    @Override
    @Transactional
    public AuthResponse updateCredentials(AtualizarCredencialesRequest request) {
        UsuarioEntity usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!usuario.getCorreo().equals(request.getCorreo())) {
            throw new RuntimeException("El correo no coincide con el usuario");
        }

        // Validar que la nueva contraseña no esté en el historial
        if (passwordManagementService.isPasswordInHistory(usuario, request.getPassword())) {
            throw new RuntimeException("No puedes usar una contraseña que ya has usado antes. Elige una nueva.");
        }

        // Guardar contraseña anterior en historial
        passwordManagementService.savePasswordToHistory(usuario, usuario.getPasswordHash());

        // Actualizar contraseña
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Establecer nueva expiración (90 días)
        passwordManagementService.setPasswordExpiry(usuario);

        usuarioRepository.save(usuario);
        
        return buildAuthResponse(usuario, "Credenciales actualizadas exitosamente");
    }

    @Override
    @Transactional
    public AuthResponse logout(LogoutRequest request) {
        // Agregar el token a la blacklist
        tokenBlacklistService.blacklistToken(request.getToken());

        // Devolver una respuesta genérica ya que no necesitamos el usuario específico
        return AuthResponse.builder()
                .mensaje("Logout exitoso - token invalidado")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse deleteAccount(EliminarCuentaRequest request) {
        UsuarioEntity usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!usuario.getCorreo().equals(request.getCorreo())) {
            throw new RuntimeException("El correo no coincide con el usuario");
        }
        
        usuarioRepository.delete(usuario);
        
        return buildAuthResponse(usuario, "Cuenta eliminada exitosamente");
    }

    @Override
    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar token de recuperación
        String resetToken = passwordManagementService.generatePasswordResetToken(usuario);

        // Enviar correo con link de recuperación
        String resetLink = "https://tu-dominio.com/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(usuario.getCorreo(), resetLink);

        return AuthResponse.builder()
                .mensaje("Se ha enviado un correo de recuperación. Revisa tu bandeja de entrada.")
                .build();
    }

    @Override
    public AuthResponse validateResetToken(ValidateResetTokenRequest request) {
        PasswordResetTokenEntity token = passwordManagementService.validateResetToken(request.getToken());

        if (token == null) {
            throw new RuntimeException("Token inválido o expirado");
        }

        return AuthResponse.builder()
                .mensaje("Token válido")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        // Validar token
        PasswordResetTokenEntity resetToken = passwordManagementService.validateResetToken(request.getToken());
        if (resetToken == null) {
            throw new RuntimeException("Token inválido o expirado");
        }

        UsuarioEntity usuario = resetToken.getUsuario();

        // Validar que no reutilice contraseña anterior
        if (passwordManagementService.isPasswordInHistory(usuario, request.getNewPassword())) {
            throw new RuntimeException("No puedes usar una contraseña que ya has usado antes");
        }

        // Guardar contraseña nueva en historial
        passwordManagementService.savePasswordToHistory(usuario, usuario.getPasswordHash());

        // Actualizar contraseña
        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // Restablecer condiciones de bloqueo y contador de fallos
        usuario.setCuentaBloqueada(false);
        usuario.setIntentosFallidos(0);

        // Establecer expiración de contraseña (90 días)
        passwordManagementService.setPasswordExpiry(usuario);

        usuarioRepository.save(usuario);

        // Marcar token como usado
        passwordManagementService.invalidateResetToken(resetToken);

        return AuthResponse.builder()
                .correo(usuario.getCorreo())
                .mensaje("Contraseña actualizada exitosamente. Por favor inicia sesión.")
                .build();
    }
}
