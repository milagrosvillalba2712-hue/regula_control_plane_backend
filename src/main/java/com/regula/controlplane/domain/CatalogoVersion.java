package com.regula.controlplane.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "catalogo_version")
public class CatalogoVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogo_id")
    private CatalogoPublicado catalogo;
    private String version;
    private String sha256;
    @Column(name = "items_json", columnDefinition = "jsonb")
    private String itemsJson;
    @Column(name = "publicado_en")
    private OffsetDateTime publicadoEn;
    private Boolean activo;

    public Long getId() { return id; }
    public CatalogoPublicado getCatalogo() { return catalogo; }
    public String getVersion() { return version; }
    public String getSha256() { return sha256; }
    public String getItemsJson() { return itemsJson; }
    public Boolean getActivo() { return activo; }
}

