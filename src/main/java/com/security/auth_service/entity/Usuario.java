package com.security.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuarios", schema = "seguridad_ms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "cuenta_bloqueada")
    private Boolean cuentaBloqueada;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles", schema = "seguridad_ms", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (cuentaBloqueada == null) {
            cuentaBloqueada = false;
        }
    }
}
