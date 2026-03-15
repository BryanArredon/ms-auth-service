package com.security.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", schema = "seguridad_ms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id")
    private Aplicacion aplicacion;

    @Column(name = "nombre_rol", nullable = false)
    private String nombreRol;
}
