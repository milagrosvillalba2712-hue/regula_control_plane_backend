package com.regula.controlplane.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ControlPlaneController {

    @PostMapping("/api/v1/licencias/validar")
    public ResponseEntity<Map<String, Object>> validarLicencia(@RequestBody Map<String, Object> body) {
        String instalacionId = String.valueOf(body.getOrDefault("instalacionId", UUID.randomUUID().toString()));
        return ResponseEntity.ok(Map.of(
                "instalacionId", instalacionId,
                "estado", "VALIDO",
                "plan", "ESTANDAR",
                "leaseRenewalAvailable", true,
                "vence", OffsetDateTime.now(ZoneOffset.UTC).plusDays(15).toString(),
                "graceUntil", OffsetDateTime.now(ZoneOffset.UTC).plusDays(30).toString(),
                "mensaje", "Validacion central demo recibida"
        ));
    }

    @PostMapping("/api/v1/telemetry/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
                "instalacionId", body.getOrDefault("instalacionId", "desconocida"),
                "estado", "OPERATIVA",
                "serverTime", OffsetDateTime.now(ZoneOffset.UTC).toString(),
                "leaseRenewalAvailable", true
        ));
    }

    @PostMapping("/api/v1/telemetry/usage")
    public ResponseEntity<Map<String, Object>> usage(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
                "instalacionId", body.getOrDefault("instalacionId", "desconocida"),
                "estado", "RECIBIDO",
                "serverTime", OffsetDateTime.now(ZoneOffset.UTC).toString()
        ));
    }

    @GetMapping("/api/v1/catalogs/manifest")
    public ResponseEntity<Map<String, Object>> manifest() {
        return ResponseEntity.ok(Map.of(
                "packageVersion", "2026.08-control-plane-demo",
                "generatedAt", OffsetDateTime.now(ZoneOffset.UTC).toString(),
                "catalogs", List.of(
                        catalog("PAISES_ISO", "2026.08.01", "sha256-demo-paises"),
                        catalog("MONEDAS_ISO", "2026.08.01", "sha256-demo-monedas"),
                        catalog("PAISES_RIESGO", "2026.08.01", "sha256-demo-riesgo"),
                        catalog("LISTAS_RIESGO_DEMO", "2026.08.01", "sha256-demo-listas")
                )
        ));
    }

    @GetMapping("/api/v1/catalogs/{code}/versions/{version}")
    public ResponseEntity<Map<String, Object>> catalogVersion(@PathVariable String code, @PathVariable String version) {
        return ResponseEntity.ok(Map.of(
                "code", code,
                "version", version,
                "hash", "sha256-demo-" + code.toLowerCase(),
                "items", List.of(
                        Map.of("codigo", code + "_001", "descripcion", "Item demo publicado por Regula", "estado", "ACTIVO"),
                        Map.of("codigo", code + "_002", "descripcion", "Item demo de prueba academica", "estado", "ACTIVO")
                )
        ));
    }

    @GetMapping("/api/v1/configuration/package")
    public ResponseEntity<Map<String, Object>> configurationPackage() {
        return ResponseEntity.ok(Map.of(
                "plan", "ESTANDAR",
                "modules", List.of("TRANSACCIONES", "ALERTAS", "KYC", "REGLAS", "REPORTES"),
                "limits", Map.of("users", 50, "transactionsMonth", 250000, "kycMonth", 10000, "reportsMonth", 1000, "rules", 40),
                "jobs", Map.of("heartbeatCron", "0 */15 * * * *", "usageSyncCron", "0 5 * * * *", "catalogSyncCron", "0 0 2 * * *")
        ));
    }

    @GetMapping("/api/admin/companies")
    public ResponseEntity<List<Map<String, Object>>> companies() {
        return ResponseEntity.ok(List.of(Map.of(
                "id", "00000000-0000-0000-0000-000000000001",
                "codigo", "FIN-SANTA-CLARA",
                "nombre", "Financiera Santa Clara",
                "estado", "ACTIVA",
                "plan", "ESTANDAR"
        )));
    }

    @GetMapping("/api/admin/installations")
    public ResponseEntity<List<Map<String, Object>>> installations() {
        return ResponseEntity.ok(List.of(Map.of(
                "id", "demo-installation-001",
                "empresa", "Financiera Santa Clara",
                "estado", "OPERATIVA",
                "ultimoHeartbeat", OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(3).toString()
        )));
    }

    @GetMapping("/api/admin/plans")
    public ResponseEntity<List<Map<String, Object>>> plans() {
        return ResponseEntity.ok(List.of(
                Map.of("codigo", "BASICO", "nombre", "Basico", "users", 10, "transactionsMonth", 50000),
                Map.of("codigo", "ESTANDAR", "nombre", "Estandar", "users", 50, "transactionsMonth", 250000),
                Map.of("codigo", "PREMIUM", "nombre", "Premium", "users", 200, "transactionsMonth", 1000000)
        ));
    }

    private Map<String, Object> catalog(String code, String version, String hash) {
        return Map.of("code", code, "version", version, "sha256", hash,
                "downloadUrl", "/api/v1/catalogs/" + code + "/versions/" + version);
    }
}

