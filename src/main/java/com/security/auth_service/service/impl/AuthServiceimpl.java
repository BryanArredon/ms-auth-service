package com.security.auth_service.service.impl;

import com.security.auth_service.dto.AuthResponse;
import com.security.auth_service.dto.LoginRequest;
import com.security.auth_service.dto.RegisterRequest;
import com.security.auth_service.entity.Usuario;
import com.security.auth_service.repository.UsuarioRepository;
import com.security.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceimpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario nuevoUsuario = Usuario.builder()
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        usuarioRepository.save(nuevoUsuario);

        return AuthResponse.builder()
                .userId(nuevoUsuario.getId())
                .correo(nuevoUsuario.getCorreo())
                .mensaje("Registro exitoso")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        if (usuario.getCuentaBloqueada() != null && usuario.getCuentaBloqueada()) {
            throw new RuntimeException("Su cuenta está bloqueada");
        }

        return AuthResponse.builder()
                .userId(usuario.getId())
                .correo(usuario.getCorreo())
                .mensaje("Login exitoso")
                .build();
    }
}
