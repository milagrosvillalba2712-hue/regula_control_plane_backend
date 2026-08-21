package com.regula.controlplane.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regula.controlplane.domain.EmpresaCliente;
import com.regula.controlplane.domain.PagoCliente;
import com.regula.controlplane.domain.PlanComercial;
import com.regula.controlplane.domain.SuscripcionCliente;
import com.regula.controlplane.repo.EmpresaClienteRepository;
import com.regula.controlplane.repo.PagoClienteRepository;
import com.regula.controlplane.repo.SuscripcionClienteRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class StripeBillingService {

    private static final Logger log = LoggerFactory.getLogger(StripeBillingService.class);

    private final EmpresaClienteRepository empresas;
    private final SuscripcionClienteRepository suscripciones;
    private final PagoClienteRepository pagos;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String secretKey;
    private final String webhookSecret;
    private final String defaultCurrency;
    private final String defaultSuccessUrl;
    private final String defaultCancelUrl;
    private final String allowedReturnOrigin;

    public StripeBillingService(EmpresaClienteRepository empresas,
                                SuscripcionClienteRepository suscripciones,
                                PagoClienteRepository pagos,
                                ObjectMapper objectMapper,
                                @Value("${regula.payments.stripe.enabled:false}") boolean enabled,
                                @Value("${regula.payments.stripe.secret-key:}") String secretKey,
                                @Value("${regula.payments.stripe.webhook-secret:}") String webhookSecret,
                                @Value("${regula.payments.stripe.default-currency:pyg}") String defaultCurrency,
                                @Value("${regula.payments.stripe.success-url}") String defaultSuccessUrl,
                                @Value("${regula.payments.stripe.cancel-url}") String defaultCancelUrl,
                                @Value("${regula.payments.stripe.allowed-return-origin}") String allowedReturnOrigin) {
        this.empresas = empresas;
        this.suscripciones = suscripciones;
        this.pagos = pagos;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.secretKey = secretKey;
        this.webhookSecret = webhookSecret;
        this.defaultCurrency = defaultCurrency;
        this.defaultSuccessUrl = defaultSuccessUrl;
        this.defaultCancelUrl = defaultCancelUrl;
        this.allowedReturnOrigin = allowedReturnOrigin;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> pagosEmpresa(UUID empresaId) {
        return pagos.findByEmpresaIdOrderByFechaHoraCreacionDesc(empresaId).stream()
                .map(this::pagoDto)
                .toList();
    }

    @Transactional
    public Map<String, Object> crearCheckout(UUID empresaId, Long suscripcionId, String successUrl, String cancelUrl) {
        return crearCheckout(empresaId, suscripcionId, null, null, null, "SUSCRIPCION", null, null, successUrl, cancelUrl);
    }

    @Transactional
    public Map<String, Object> crearCheckout(UUID empresaId,
                                             Long suscripcionId,
                                             BigDecimal monto,
                                             String moneda,
                                             String concepto,
                                             String tipo,
                                             String referenciaExterna,
                                             Map<String, Object> metadata,
                                             String successUrl,
                                             String cancelUrl) {
        if (!enabled || !StringUtils.hasText(secretKey)) {
            return Map.of(
                    "online", false,
                    "estado", "STRIPE_NO_CONFIGURADO",
                    "mensaje", "Stripe no esta habilitado en el Control Plane. Configure STRIPE_ENABLED y STRIPE_SECRET_KEY."
            );
        }
        EmpresaCliente empresa = empresas.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        boolean pagoUnico = monto != null && monto.compareTo(BigDecimal.ZERO) > 0;
        SuscripcionCliente suscripcion = !pagoUnico && suscripcionId != null
                ? suscripciones.findById(suscripcionId).orElseThrow(() -> new IllegalArgumentException("Suscripcion no encontrada"))
                : !pagoUnico ? suscripciones.findTopByEmpresaIdOrderByFechaFinDesc(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene suscripcion")) : null;
        if (suscripcion != null && !empresa.getId().equals(suscripcion.getEmpresa().getId())) {
            throw new IllegalArgumentException("La suscripcion no corresponde a la empresa indicada");
        }

        PlanComercial plan = suscripcion != null ? suscripcion.getPlan() : null;
        BigDecimal amount = pagoUnico ? monto : plan != null && plan.getPrecioAnual() != null ? plan.getPrecioAnual() : BigDecimal.ZERO;
        String currency = resolveCurrency(moneda, amount, pagoUnico);
        String tipoPago = StringUtils.hasText(tipo) ? tipo : pagoUnico ? "USUARIOS_ADICIONALES" : "SUSCRIPCION";
        String descripcion = StringUtils.hasText(concepto) ? concepto : plan != null ? "Regula " + plan.getNombre() : "Regula";
        PagoCliente pago = new PagoCliente();
        pago.setEmpresa(empresa);
        pago.setSuscripcion(suscripcion);
        pago.setCodigo("STRIPE-" + empresa.getCodigo() + "-" + System.currentTimeMillis());
        pago.setMonto(amount);
        pago.setMoneda(currency.toUpperCase(Locale.ROOT));
        pago.setEstado("PENDIENTE");
        pago.setMetodoPago("STRIPE_CHECKOUT");
        pago.setProveedorPago("STRIPE");
        pago.setDetalleJson(safeJson(Map.of(
                "tipo", tipoPago,
                "concepto", descripcion,
                "referenciaExterna", referenciaExterna != null ? referenciaExterna : "",
                "metadata", metadata != null ? metadata : Map.of(),
                "plan", plan != null ? plan.getCodigo() : ""
        )));
        pago = pagos.save(pago);

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(pagoUnico ? SessionCreateParams.Mode.PAYMENT : SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(addCheckoutSessionParam(safeReturnUrl(successUrl, defaultSuccessUrl)))
                    .setCancelUrl(safeReturnUrl(cancelUrl, defaultCancelUrl))
                    .setClientReferenceId(String.valueOf(pago.getId()))
                    .putMetadata("pagoId", String.valueOf(pago.getId()))
                    .putMetadata("empresaId", empresa.getId().toString())
                    .putMetadata("tipo", tipoPago);
            if (suscripcion != null) {
                builder.putMetadata("suscripcionId", String.valueOf(suscripcion.getId()));
            }
            if (plan != null) {
                builder.putMetadata("planCodigo", plan.getCodigo());
            }
            if (StringUtils.hasText(referenciaExterna)) {
                builder.putMetadata("referenciaExterna", referenciaExterna);
            }
            if (metadata != null) {
                metadata.forEach((key, value) -> {
                    if (key != null && value != null) {
                        builder.putMetadata(String.valueOf(key), String.valueOf(value));
                    }
                });
            }

            if (!pagoUnico && suscripcion != null) {
                builder.setSubscriptionData(SessionCreateParams.SubscriptionData.builder()
                        .putMetadata("empresaId", empresa.getId().toString())
                        .putMetadata("suscripcionId", String.valueOf(suscripcion.getId()))
                        .putMetadata("pagoId", String.valueOf(pago.getId()))
                        .build());
            }

            if (!pagoUnico && plan != null && StringUtils.hasText(plan.getStripePriceId())) {
                builder.addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(plan.getStripePriceId())
                        .setQuantity(1L)
                        .build());
            } else {
                builder.addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(toStripeAmount(amount, currency))
                                .setRecurring(pagoUnico ? null : SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                        .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.YEAR)
                                        .build())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(descripcion)
                                        .putMetadata("tipo", tipoPago)
                                        .build())
                                .build())
                        .build());
            }

            Session session = Session.create(builder.build(), RequestOptions.builder().setApiKey(secretKey).build());
            pago.setStripeCheckoutSessionId(session.getId());
            pago.setStripeCustomerId(session.getCustomer());
            pago.setStripeSubscriptionId(session.getSubscription());
            pago.setCheckoutUrl(session.getUrl());
            pago.setDetalleJson(safeJson(Map.of(
                    "checkoutSessionId", session.getId(),
                    "mode", pagoUnico ? "payment" : "subscription",
                    "tipo", tipoPago,
                    "concepto", descripcion,
                    "referenciaExterna", referenciaExterna != null ? referenciaExterna : "",
                    "plan", plan != null ? plan.getCodigo() : ""
            )));
            pago = pagos.save(pago);
            return pagoDto(pago);
        } catch (Exception ex) {
            log.warn("[STRIPE] No se pudo crear Checkout para empresa={}, tipo={}, moneda={}, monto={}, error={}: {}",
                    empresa.getId(), tipoPago, currency, amount, ex.getClass().getSimpleName(), ex.getMessage());
            pago.setEstado("ERROR");
            pago.setDetalleJson(safeJson(Map.of("error", ex.getClass().getSimpleName(), "mensaje", ex.getMessage())));
            pagos.save(pago);
            throw new IllegalStateException("No se pudo crear la sesion de Stripe Checkout", ex);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pagoPorCheckoutSession(String sessionId) {
        PagoCliente pago = pagos.findByStripeCheckoutSessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Pago Stripe no encontrado para la sesion indicada"));
        return pagoDto(pago);
    }

    @Transactional
    public Map<String, Object> procesarWebhook(String payload, String signature) {
        if (!StringUtils.hasText(webhookSecret)) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET no configurado");
        }
        Event event = verifyEvent(payload, signature);
        try {
            JsonNode object = objectMapper.readTree(payload).path("data").path("object");
            switch (event.getType()) {
                case "checkout.session.completed" -> marcarCheckoutCompletado(object);
                case "invoice.paid", "invoice.payment_succeeded" -> marcarFacturaPagada(object);
                case "invoice.payment_failed" -> marcarPagoFallido(object);
                case "customer.subscription.deleted" -> marcarSuscripcionStripe(object, "CANCELADA");
                case "customer.subscription.updated" -> marcarSuscripcionStripe(object, "ACTUALIZADA");
                default -> {
                    return Map.of("recibido", true, "ignorado", true, "tipo", event.getType());
                }
            }
            return Map.of("recibido", true, "tipo", event.getType());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo procesar evento Stripe " + event.getType(), ex);
        }
    }

    private Event verifyEvent(String payload, String signature) {
        try {
            return Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new IllegalArgumentException("Firma Stripe invalida", ex);
        }
    }

    private void marcarCheckoutCompletado(JsonNode object) {
        String sessionId = text(object, "id");
        PagoCliente pago = pagos.findByStripeCheckoutSessionId(sessionId)
                .orElseGet(() -> findByMetadataPagoId(object));
        pago.setEstado("CONFIRMADO");
        pago.setFechaPago(OffsetDateTime.now(ZoneOffset.UTC));
        pago.setStripeCustomerId(firstText(text(object, "customer"), pago.getStripeCustomerId()));
        pago.setStripeSubscriptionId(firstText(text(object, "subscription"), pago.getStripeSubscriptionId()));
        pago.setStripePaymentIntentId(firstText(text(object, "payment_intent"), pago.getStripePaymentIntentId()));
        pago.setDetalleJson(safeJson(Map.of("evento", "checkout.session.completed", "sessionId", sessionId)));
        pagos.save(pago);
    }

    private void marcarFacturaPagada(JsonNode object) {
        String subscription = text(object, "subscription");
        PagoCliente pago = pagos.findTopByStripeSubscriptionIdOrderByFechaHoraCreacionDesc(subscription)
                .orElseGet(() -> findByMetadataPagoId(object));
        pago.setEstado("CONFIRMADO");
        pago.setFechaPago(OffsetDateTime.now(ZoneOffset.UTC));
        pago.setStripeInvoiceId(firstText(text(object, "id"), pago.getStripeInvoiceId()));
        pago.setStripeCustomerId(firstText(text(object, "customer"), pago.getStripeCustomerId()));
        pago.setDetalleJson(safeJson(Map.of("evento", "invoice.paid", "invoiceId", text(object, "id"))));
        pagos.save(pago);
    }

    private void marcarPagoFallido(JsonNode object) {
        PagoCliente pago = pagos.findTopByStripeSubscriptionIdOrderByFechaHoraCreacionDesc(text(object, "subscription"))
                .orElseGet(() -> findByMetadataPagoId(object));
        pago.setEstado("RECHAZADO");
        pago.setStripeInvoiceId(firstText(text(object, "id"), pago.getStripeInvoiceId()));
        pago.setDetalleJson(safeJson(Map.of("evento", "invoice.payment_failed", "invoiceId", text(object, "id"))));
        pagos.save(pago);
    }

    private void marcarSuscripcionStripe(JsonNode object, String estado) {
        String subscriptionId = text(object, "id");
        pagos.findTopByStripeSubscriptionIdOrderByFechaHoraCreacionDesc(subscriptionId).ifPresent(pago -> {
            pago.setDetalleJson(safeJson(Map.of("evento", "customer.subscription", "estadoStripe", text(object, "status"))));
            if ("CANCELADA".equals(estado)) {
                pago.setEstado("CANCELADO");
            }
            pagos.save(pago);
        });
    }

    private PagoCliente findByMetadataPagoId(JsonNode object) {
        Long pagoId = longValue(object.path("metadata").path("pagoId").asText(null));
        if (pagoId == null) {
            throw new IllegalArgumentException("Evento Stripe sin metadata.pagoId reconciliable");
        }
        return pagos.findById(pagoId).orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
    }

    private Map<String, Object> pagoDto(PagoCliente pago) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pago.getId());
        map.put("empresaId", pago.getEmpresa() != null ? pago.getEmpresa().getId() : null);
        map.put("suscripcionId", pago.getSuscripcion() != null ? pago.getSuscripcion().getId() : null);
        map.put("codigo", pago.getCodigo());
        map.put("monto", pago.getMonto());
        map.put("moneda", pago.getMoneda());
        map.put("estado", pago.getEstado());
        map.put("metodoPago", pago.getMetodoPago());
        map.put("proveedorPago", pago.getProveedorPago());
        map.put("checkoutUrl", pago.getCheckoutUrl());
        map.put("stripeCheckoutSessionId", pago.getStripeCheckoutSessionId());
        map.put("stripeSubscriptionId", pago.getStripeSubscriptionId());
        map.put("fechaPago", pago.getFechaPago());
        map.put("fechaVencimiento", pago.getFechaVencimiento());
        map.put("fechaCreacion", pago.getFechaHoraCreacion());
        return map;
    }

    private String safeReturnUrl(String candidate, String fallback) {
        if (!StringUtils.hasText(candidate)) {
            return fallback;
        }
        return candidate.startsWith(allowedReturnOrigin) ? candidate : fallback;
    }

    private String addCheckoutSessionParam(String url) {
        if (url.contains("{CHECKOUT_SESSION_ID}")) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + "session_id={CHECKOUT_SESSION_ID}";
    }

    private long toStripeAmount(BigDecimal amount, String currency) {
        if ("pyg".equalsIgnoreCase(currency)) {
            return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        }
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String resolveCurrency(String requestedCurrency, BigDecimal amount, boolean pagoUnico) {
        if (StringUtils.hasText(requestedCurrency)) {
            return requestedCurrency.toLowerCase(Locale.ROOT);
        }
        String configured = defaultCurrency.toLowerCase(Locale.ROOT);
        if (!pagoUnico && "usd".equals(configured) && amount != null
                && amount.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0) {
            return "pyg";
        }
        return configured;
    }

    private String safeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private String firstText(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private Long longValue(String value) {
        try {
            return StringUtils.hasText(value) ? Long.parseLong(value) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
