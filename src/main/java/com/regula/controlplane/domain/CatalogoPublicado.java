package com.regula.controlplane.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_publicado")
public class CatalogoPublicado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Boolean getActivo() { return activo; }
}

