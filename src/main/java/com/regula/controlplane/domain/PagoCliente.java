package com.regula.controlplane.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pago_cliente")
public class PagoCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaCliente empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    private SuscripcionCliente suscripcion;

    private String codigo;
    private BigDecimal monto;
    private String moneda;

    @Column(name = "fecha_pago")
    private OffsetDateTime fechaPago;

    private String estado;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "proveedor_pago")
    private String proveedorPago;

    @Column(name = "stripe_checkout_session_id")
    private String stripeCheckoutSessionId;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "stripe_invoice_id")
    private String stripeInvoiceId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "checkout_url")
    private String checkoutUrl;

    @Column(name = "fecha_vencimiento")
    private OffsetDateTime fechaVencimiento;

    @Column(name = "detalle_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String detalleJson;

    @Column(name = "fecha_hora_creacion")
    private OffsetDateTime fechaHoraCreacion;

    @Column(name = "fecha_hora_modificacion")
    private OffsetDateTime fechaHoraModificacion;

    @PrePersist
    void prePersist() {
        if (fechaHoraCreacion == null) {
            fechaHoraCreacion = OffsetDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        fechaHoraModificacion = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public EmpresaCliente getEmpresa() { return empresa; }
    public SuscripcionCliente getSuscripcion() { return suscripcion; }
    public String getCodigo() { return codigo; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public OffsetDateTime getFechaPago() { return fechaPago; }
    public String getEstado() { return estado; }
    public String getMetodoPago() { return metodoPago; }
    public String getProveedorPago() { return proveedorPago; }
    public String getStripeCheckoutSessionId() { return stripeCheckoutSessionId; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public String getStripeInvoiceId() { return stripeInvoiceId; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public OffsetDateTime getFechaVencimiento() { return fechaVencimiento; }
    public String getDetalleJson() { return detalleJson; }
    public OffsetDateTime getFechaHoraCreacion() { return fechaHoraCreacion; }
    public OffsetDateTime getFechaHoraModificacion() { return fechaHoraModificacion; }

    public void setEmpresa(EmpresaCliente empresa) { this.empresa = empresa; }
    public void setSuscripcion(SuscripcionCliente suscripcion) { this.suscripcion = suscripcion; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public void setFechaPago(OffsetDateTime fechaPago) { this.fechaPago = fechaPago; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public void setProveedorPago(String proveedorPago) { this.proveedorPago = proveedorPago; }
    public void setStripeCheckoutSessionId(String stripeCheckoutSessionId) { this.stripeCheckoutSessionId = stripeCheckoutSessionId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }
    public void setStripeSubscriptionId(String stripeSubscriptionId) { this.stripeSubscriptionId = stripeSubscriptionId; }
    public void setStripeInvoiceId(String stripeInvoiceId) { this.stripeInvoiceId = stripeInvoiceId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public void setFechaVencimiento(OffsetDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public void setDetalleJson(String detalleJson) { this.detalleJson = detalleJson; }
    public void setFechaHoraModificacion(OffsetDateTime fechaHoraModificacion) { this.fechaHoraModificacion = fechaHoraModificacion; }
}
