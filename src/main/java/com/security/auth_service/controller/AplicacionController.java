package com.security.auth_service.controller;

import com.security.auth_service.dto.CrearAplicacionRequest;
import com.security.auth_service.entity.AplicacionEntity;
import com.security.auth_service.service.AplicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/aplicaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AplicacionController {

    private final AplicacionService aplicacionService;

    @GetMapping
    public ResponseEntity<List<AplicacionEntity>> listarTodas() {
        return ResponseEntity.ok(aplicacionService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AplicacionEntity> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(aplicacionService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AplicacionEntity> crear(@RequestBody CrearAplicacionRequest request) {
        return ResponseEntity.ok(aplicacionService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AplicacionEntity> actualizar(@PathVariable UUID id, @RequestBody AplicacionEntity aplicacion) {
        return ResponseEntity.ok(aplicacionService.actualizar(id, aplicacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        aplicacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
