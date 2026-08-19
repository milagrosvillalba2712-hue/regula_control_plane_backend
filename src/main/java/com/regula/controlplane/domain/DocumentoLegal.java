package com.regula.controlplane.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "documento_legal", uniqueConstraints = {
        @UniqueConstraint(name = "uk_documento_legal_version", columnNames = {"tipo", "version"})
})
public class DocumentoLegal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "url_documento", length = 500)
    private String urlDocumento;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_publicacion")
    private OffsetDateTime fechaPublicacion;

    @Column(name = "fecha_hora_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaHoraCreacion;

    @Column(name = "fecha_hora_modificacion")
    private OffsetDateTime fechaHoraModificacion;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (fechaCreacion == null) fechaCreacion = now;
        if (fechaHoraCreacion == null) fechaHoraCreacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaHoraModificacion = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public Integer getVersion() { return version; }
    public String getTitulo() { return titulo; }
    public String getContenido() { return contenido; }
    public String getUrlDocumento() { return urlDocumento; }
    public Boolean getActivo() { return activo; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaPublicacion() { return fechaPublicacion; }
    public OffsetDateTime getFechaHoraCreacion() { return fechaHoraCreacion; }
    public OffsetDateTime getFechaHoraModificacion() { return fechaHoraModificacion; }

    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setVersion(Integer version) { this.version = version; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setUrlDocumento(String urlDocumento) { this.urlDocumento = urlDocumento; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public void setFechaPublicacion(OffsetDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
}
