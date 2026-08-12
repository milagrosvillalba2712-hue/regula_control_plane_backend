package com.regula.controlplane.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.regula.controlplane.domain.InstalacionCliente;
import com.regula.controlplane.domain.LeaseEmitido;
import com.regula.controlplane.domain.PlanComercial;
import com.regula.controlplane.domain.SuscripcionCliente;
import com.regula.controlplane.repo.LeaseEmitidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LeaseSigningService {
    private final LeaseEmitidoRepository leases;
    private final ObjectMapper objectMapper;
    private final KeyPair keyPair;
    private final String kid;

    public LeaseSigningService(LeaseEmitidoRepository leases,
                               @Value("${regula.control-plane.license-signing.kid:regula-demo-rs256-2026-08}") String kid) {
        this.leases = leases;
        this.kid = kid;
        this.objectMapper = new ObjectMapper()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.keyPair = generateEphemeralKeyPair();
    }

    @Transactional
    public LeaseTokenResponse issueLease(InstalacionCliente instalacion, SuscripcionCliente suscripcion) {
        PlanComercial plan = suscripcion.getPlan();
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = suscripcion.getFechaFin().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        OffsetDateTime graceUntil = suscripcion.getGraceUntil().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        UUID nonce = UUID.randomUUID();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", "regula-control-plane");
        payload.put("sub", instalacion.getEmpresa().getId().toString());
        payload.put("empresaNombre", instalacion.getEmpresa().getNombre());
        payload.put("instalacionId", instalacion.getId().toString());
        payload.put("hardwareId", instalacion.getFingerprintHash());
        payload.put("plan", plan.getCodigo());
        payload.put("maxUsers", plan.getLimiteUsuarios());
        payload.put("maxTransactionsMonth", plan.getLimiteTransaccionesMes());
        payload.put("maxKycMonth", plan.getLimiteKycMes());
        payload.put("maxReportsMonth", plan.getLimiteReportesMes());
        payload.put("maxRules", plan.getLimiteReglas());
        payload.put("modules", parseJson(plan.getModulosJson()));
        payload.put("issuedAt", issuedAt.toEpochSecond());
        payload.put("expiresAt", expiresAt.toEpochSecond());
        payload.put("graceUntil", graceUntil.toEpochSecond());
        payload.put("nonce", nonce.toString());

        Map<String, Object> header = Map.of("alg", "RS256", "typ", "REGULA-LEASE", "kid", kid);
        String headerPart = base64Url(toJson(header).getBytes(StandardCharsets.UTF_8));
        String payloadJson = toJson(payload);
        String payloadPart = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerPart + "." + payloadPart;
        String signature = sign(signingInput);
        String token = signingInput + "." + signature;

        LeaseEmitido lease = new LeaseEmitido();
        lease.setInstalacion(instalacion);
        lease.setSuscripcion(suscripcion);
        lease.setEstado("EMITIDO");
        lease.setPlanCodigo(plan.getCodigo());
        lease.setEmitidoEn(issuedAt);
        lease.setVenceEn(expiresAt);
        lease.setGraceUntil(graceUntil);
        lease.setKid(kid);
        lease.setNonce(nonce);
        lease.setLeasePayload(payloadJson);
        lease.setFirma(signature);
        leases.save(lease);

        return new LeaseTokenResponse("REGULA_LEASE_JWS", token, kid, "RS256", issuedAt, expiresAt, graceUntil, payload);
    }

    public Map<String, Object> jwks() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", kid);
        jwk.put("n", base64UrlUnsigned(publicKey.getModulus().toByteArray()));
        jwk.put("e", base64UrlUnsigned(publicKey.getPublicExponent().toByteArray()));
        return Map.of("keys", java.util.List.of(jwk));
    }

    private KeyPair generateEphemeralKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo generar la clave RSA de firma de licencias", ex);
        }
    }

    private String sign(String signingInput) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
            return base64Url(signature.sign());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo firmar el lease", ex);
        }
    }

    private Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException ex) {
            return java.util.List.of();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el lease", ex);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String base64UrlUnsigned(byte[] bytes) {
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] unsigned = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
            return base64Url(unsigned);
        }
        return base64Url(bytes);
    }
}
