package com.regula.controlplane.api;

import com.regula.controlplane.service.StripeBillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

@RestController
public class StripeBillingController {

    private final StripeBillingService stripeBillingService;

    public StripeBillingController(StripeBillingService stripeBillingService) {
        this.stripeBillingService = stripeBillingService;
    }

    @GetMapping("/api/admin/payments")
    public ResponseEntity<List<Map<String, Object>>> pagosEmpresa(@RequestParam UUID empresaId) {
        return ResponseEntity.ok(stripeBillingService.pagosEmpresa(empresaId));
    }

    @PostMapping("/api/v1/billing/checkout-session")
    public ResponseEntity<Map<String, Object>> crearCheckout(@RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(stripeBillingService.crearCheckout(
                request.empresaId(),
                request.suscripcionId(),
                request.monto(),
                request.moneda(),
                request.concepto(),
                request.tipo(),
                request.referenciaExterna(),
                request.metadata(),
                request.successUrl(),
                request.cancelUrl()
        ));
    }

    @GetMapping("/api/v1/billing/checkout-session/{sessionId}")
    public ResponseEntity<Map<String, Object>> consultarCheckout(@PathVariable String sessionId) {
        return ResponseEntity.ok(stripeBillingService.pagoPorCheckoutSession(sessionId));
    }

    @PostMapping("/api/public/stripe/webhook")
    public ResponseEntity<Map<String, Object>> stripeWebhook(@RequestBody String payload,
                                                            @RequestHeader("Stripe-Signature") String signature) {
        return ResponseEntity.ok(stripeBillingService.procesarWebhook(payload, signature));
    }

    public record CheckoutRequest(UUID empresaId,
                                  Long suscripcionId,
                                  BigDecimal monto,
                                  String moneda,
                                  String concepto,
                                  String tipo,
                                  String referenciaExterna,
                                  Map<String, Object> metadata,
                                  String successUrl,
                                  String cancelUrl) {
    }
}
