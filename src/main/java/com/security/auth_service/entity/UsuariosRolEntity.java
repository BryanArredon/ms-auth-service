package com.security.auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario_roles", schema = "seguridad_ms")
@Data
public class UsuariosRolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id")
    private RolEntity rol;
}
