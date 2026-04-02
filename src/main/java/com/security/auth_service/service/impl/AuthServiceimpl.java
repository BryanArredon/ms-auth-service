package com.security.auth_service.service.impl;

import com.security.auth_service.dto.AtualizarCredencialesRequest;
import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.EliminarCuentaRequest;
import com.security.auth_service.dto.LogoutRequest;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.RegisterRequest;
import com.security.auth_service.entity.UsuarioEntity;
import com.security.auth_service.repository.UsuarioRepository;
import com.security.auth_service.service.AuthService;
import com.security.auth_service.service.JwtService;
import com.security.auth_service.service.TokenBlacklistService;
import com.security.auth_service.service.EmailService;
import com.security.auth_service.utils.OtpGenerator;
import com.security.auth_service.service.TotpService;
import com.security.auth_service.entity.MfaOtpeEntity;
import com.security.auth_service.repository.MfaOtpeRepository;
import com.security.auth_service.dto.VerifyMfaRequest;
import com.security.auth_service.dto.MfaSetupResponse;
import com.security.auth_service.dto.EnableTotpRequest;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TotpService totpService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        usuarioRepository.findByCorreo(request.getCorreo())
                .ifPresent(usuario -> 
                    { throw new RuntimeException("El correo ya está registrado"); });
        UsuarioEntity nuevoUsuario = UsuarioEntity.builder() 
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        usuarioRepository.save(nuevoUsuario);

        return buildAuthResponse(nuevoUsuario, "Registro exitoso");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UsuarioEntity user = usuarioRepository.findByCorreo(request.getCorreo())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .map(this::validarEstadoUsuario)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
                
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
    
    private UsuarioEntity validarEstadoUsuario(UsuarioEntity usuario) {     
        if (Boolean.TRUE.equals(usuario.getCuentaBloqueada())) {
            throw new RuntimeException("Su cuenta está bloqueada");
        }
        return usuario;
    }

    private AuthResponse buildAuthResponse(UsuarioEntity usuario, String mensaje) {
        String token = jwtService.generateToken(usuario.getCorreo());
        return AuthResponse.builder()
                .userId(usuario.getId())
                .correo(usuario.getCorreo())
                .token(token)
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

    public AuthResponse unblock(String correo) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setCuentaBloqueada(false);
        usuarioRepository.save(usuario);
        
        return buildAuthResponse(usuario, "Cuenta desbloqueada exitosamente");
    }

    @Transactional
    public AuthResponse updateCredentials(AtualizarCredencialesRequest request) {
        UsuarioEntity usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!usuario.getCorreo().equals(request.getCorreo())) {
            throw new RuntimeException("El correo no coincide con el usuario");
        }
        
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuarioRepository.save(usuario);
        
        return buildAuthResponse(usuario, "Credenciales actualizadas exitosamente");
    }

    @Transactional
    public AuthResponse logout(LogoutRequest request) {
        // Agregar el token a la blacklist
        tokenBlacklistService.blacklistToken(request.getToken());

        // Devolver una respuesta genérica ya que no necesitamos el usuario específico
        return AuthResponse.builder()
                .mensaje("Logout exitoso - token invalidado")
                .build();
    }

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
}
