package com.regula.controlplane.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "heartbeat_instalacion")
public class HeartbeatInstalacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instalacion_id")
    private InstalacionCliente instalacion;

    @Column(name = "estado_reportado")
    private String estadoReportado;

    @Column(name = "version_producto")
    private String versionProducto;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @Column(name = "fecha_evento")
    private OffsetDateTime fechaEvento;

    public void setInstalacion(InstalacionCliente instalacion) { this.instalacion = instalacion; }
    public void setEstadoReportado(String estadoReportado) { this.estadoReportado = estadoReportado; }
    public void setVersionProducto(String versionProducto) { this.versionProducto = versionProducto; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    public void setFechaEvento(OffsetDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
}
