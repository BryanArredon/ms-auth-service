package com.security.auth_service.controller;

import com.security.auth_service.dto.ModuloDTO;
import com.security.auth_service.dto.UserDTO;
import com.security.auth_service.service.AuthService;
import com.security.auth_service.service.ModuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final ModuloService moduloService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {
        return ResponseEntity.ok(authService.getProfile());
    }

    @GetMapping("/me/modules")
    public ResponseEntity<List<ModuloDTO>> getMyModules() {
        return ResponseEntity.ok(moduloService.getModulosPorUsuarioActual());
    }
}
