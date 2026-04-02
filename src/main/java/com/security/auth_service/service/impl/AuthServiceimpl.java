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

    public AuthResponse login(LoginRequest request) {
        return usuarioRepository.findByCorreo(request.getCorreo())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .map(this::validarEstadoUsuario)
                .map(user -> {
                    user.setUltimoAcceso(LocalDateTime.now());
                    usuarioRepository.save(user);
                    return buildAuthResponse(user, "Login exitoso");
                })
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
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
