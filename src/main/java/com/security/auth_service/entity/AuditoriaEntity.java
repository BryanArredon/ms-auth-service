package com.security.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditoria", schema = "seguridad_ms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id")
    private AplicacionEntity aplicacion;

    @Column(nullable = false)
    private String accion;

    @Column(name = "tabla_afectada")
    private String tablaAfectada;

    @Column(name = "dato_id")
    private String datoId;

    @Column(name = "valor_anterior", columnDefinition = "jsonb")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "jsonb")
    private String valorNuevo;

    @Column(name = "fecha_accion", updatable = false)
    private LocalDateTime fechaAccion;

    @PrePersist
    protected void onCreate() {
        if (fechaAccion == null) {
            fechaAccion = LocalDateTime.now();
        }
    }
}
