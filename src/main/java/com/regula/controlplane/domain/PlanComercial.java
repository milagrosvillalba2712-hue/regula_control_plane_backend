package com.regula.controlplane.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plan_comercial")
public class PlanComercial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    @Column(name = "limite_usuarios")
    private Integer limiteUsuarios;
    @Column(name = "limite_transacciones_mes")
    private Long limiteTransaccionesMes;
    @Column(name = "limite_kyc_mes")
    private Long limiteKycMes;
    @Column(name = "limite_reportes_mes")
    private Long limiteReportesMes;
    @Column(name = "limite_reglas")
    private Integer limiteReglas;
    @Column(name = "precio_anual")
    private BigDecimal precioAnual;
    @Column(name = "stripe_product_id")
    private String stripeProductId;
    @Column(name = "stripe_price_id")
    private String stripePriceId;
    @Column(name = "modulos_json", columnDefinition = "jsonb")
    private String modulosJson;
    private Boolean activo;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Integer getLimiteUsuarios() { return limiteUsuarios; }
    public Long getLimiteTransaccionesMes() { return limiteTransaccionesMes; }
    public Long getLimiteKycMes() { return limiteKycMes; }
    public Long getLimiteReportesMes() { return limiteReportesMes; }
    public Integer getLimiteReglas() { return limiteReglas; }
    public BigDecimal getPrecioAnual() { return precioAnual; }
    public String getStripeProductId() { return stripeProductId; }
    public String getStripePriceId() { return stripePriceId; }
    public String getModulosJson() { return modulosJson; }
    public Boolean getActivo() { return activo; }
}
