package com.security.auth_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.ManyToOne; 

@Entity
@Table(name = "auditoria", schema = "seguridad_ms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id")
    private AplicacionEntity aplicacion;

    @Column(name = "accion")
    private String accion;

    @Column(name = "tabla_afectada")
    private String tablaAfectada;

    @Column(name = "dato_id")
    private String datoId;

    @Column(name = "valor_anterior")
    private String valorAnterior;

    @Column(name = "valor_nuevo")
    private String valorNuevo;

    @Column(name = "fecha_accion")
    private LocalDateTime fechaAccion;
}
