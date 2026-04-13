package com.security.auth_service.controller;

import com.security.auth_service.entity.AuditoriaEntity;
import com.security.auth_service.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AuditoriaEntity>> getAuditLogs() {
        return ResponseEntity.ok(auditoriaService.listarTodas());
    }
}
