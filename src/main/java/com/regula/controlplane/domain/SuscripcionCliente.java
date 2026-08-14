package com.regula.controlplane.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suscripcion_cliente")
public class SuscripcionCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private EmpresaCliente empresa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private PlanComercial plan;
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    @Column(name = "grace_until")
    private LocalDate graceUntil;
    private String estado;
    @Column(name = "renovacion_automatica")
    private Boolean renovacionAutomatica;

    public Long getId() { return id; }
    public EmpresaCliente getEmpresa() { return empresa; }
    public PlanComercial getPlan() { return plan; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public LocalDate getGraceUntil() { return graceUntil; }
    public String getEstado() { return estado; }
}

