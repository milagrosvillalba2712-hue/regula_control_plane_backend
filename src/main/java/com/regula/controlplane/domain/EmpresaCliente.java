package com.regula.controlplane.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "empresa_cliente")
public class EmpresaCliente {
    @Id
    private UUID id;
    private String codigo;
    private String nombre;
    private String ruc;
    @Column(name = "email_contacto")
    private String emailContacto;
    private String estado;
    @Column(name = "fecha_hora_creacion")
    private OffsetDateTime fechaHoraCreacion;
    @Column(name = "fecha_hora_modificacion")
    private OffsetDateTime fechaHoraModificacion;

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public String getEmailContacto() { return emailContacto; }
    public String getEstado() { return estado; }
}

