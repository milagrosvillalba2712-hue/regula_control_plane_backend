package com.regula.controlplane.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consumo_reportado")
public class ConsumoReportado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instalacion_id")
    private InstalacionCliente instalacion;

    private String periodo;

    @Column(name = "usuarios_activos")
    private Integer usuariosActivos;

    private Long transacciones;

    @Column(name = "consultas_kyc")
    private Long consultasKyc;

    private Long reportes;
    private Integer reglas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "fecha_evento")
    private OffsetDateTime fechaEvento;

    public void setInstalacion(InstalacionCliente instalacion) { this.instalacion = instalacion; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public void setUsuariosActivos(Integer usuariosActivos) { this.usuariosActivos = usuariosActivos; }
    public void setTransacciones(Long transacciones) { this.transacciones = transacciones; }
    public void setConsultasKyc(Long consultasKyc) { this.consultasKyc = consultasKyc; }
    public void setReportes(Long reportes) { this.reportes = reportes; }
    public void setReglas(Integer reglas) { this.reglas = reglas; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public void setFechaEvento(OffsetDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
}
