package com.security.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", schema = "seguridad_ms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id")
    private AplicacionEntity aplicacion;

    @Column(name = "nombre_rol", nullable = false)
    private String nombreRol;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rol_modulos",
        schema = "seguridad_ms",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "modulo_id")
    )
    @Builder.Default
    private Set<ModuloEntity> modulos = new HashSet<>();
}
