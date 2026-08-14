package com.regula.controlplane.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lease_emitido")
public class LeaseEmitido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instalacion_id")
    private InstalacionCliente instalacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    private SuscripcionCliente suscripcion;

    private String estado;

    @Column(name = "plan_codigo")
    private String planCodigo;

    @Column(name = "emitido_en")
    private OffsetDateTime emitidoEn;

    @Column(name = "vence_en")
    private OffsetDateTime venceEn;

    @Column(name = "grace_until")
    private OffsetDateTime graceUntil;

    private String kid;
    private UUID nonce;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lease_payload", columnDefinition = "jsonb")
    private String leasePayload;

    private String firma;

    public void setInstalacion(InstalacionCliente instalacion) { this.instalacion = instalacion; }
    public void setSuscripcion(SuscripcionCliente suscripcion) { this.suscripcion = suscripcion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setPlanCodigo(String planCodigo) { this.planCodigo = planCodigo; }
    public void setEmitidoEn(OffsetDateTime emitidoEn) { this.emitidoEn = emitidoEn; }
    public void setVenceEn(OffsetDateTime venceEn) { this.venceEn = venceEn; }
    public void setGraceUntil(OffsetDateTime graceUntil) { this.graceUntil = graceUntil; }
    public void setKid(String kid) { this.kid = kid; }
    public void setNonce(UUID nonce) { this.nonce = nonce; }
    public void setLeasePayload(String leasePayload) { this.leasePayload = leasePayload; }
    public void setFirma(String firma) { this.firma = firma; }
}
