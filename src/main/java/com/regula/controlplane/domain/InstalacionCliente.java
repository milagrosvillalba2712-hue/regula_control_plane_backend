package com.regula.controlplane.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "instalacion_cliente")
public class InstalacionCliente {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private EmpresaCliente empresa;
    @Column(name = "identificador_instalacion")
    private String identificadorInstalacion;
    @Column(name = "fingerprint_hash")
    private String fingerprintHash;
    @Column(name = "version_producto")
    private String versionProducto;
    private String estado;
    @Column(name = "activada_en")
    private OffsetDateTime activadaEn;
    @Column(name = "ultimo_heartbeat_en")
    private OffsetDateTime ultimoHeartbeatEn;
    @Column(name = "clon_detectado")
    private Boolean clonDetectado;

    public UUID getId() { return id; }
    public EmpresaCliente getEmpresa() { return empresa; }
    public String getIdentificadorInstalacion() { return identificadorInstalacion; }
    public String getFingerprintHash() { return fingerprintHash; }
    public String getVersionProducto() { return versionProducto; }
    public String getEstado() { return estado; }
    public OffsetDateTime getUltimoHeartbeatEn() { return ultimoHeartbeatEn; }
}

