package com.regula.controlplane.api;

import com.regula.controlplane.domain.*;
import com.regula.controlplane.repo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ControlPlaneController {

    private final EmpresaClienteRepository empresas;
    private final PlanComercialRepository planes;
    private final SuscripcionClienteRepository suscripciones;
    private final InstalacionClienteRepository instalaciones;
    private final CatalogoVersionRepository catalogos;

    public ControlPlaneController(EmpresaClienteRepository empresas,
                                  PlanComercialRepository planes,
                                  SuscripcionClienteRepository suscripciones,
                                  InstalacionClienteRepository instalaciones,
                                  CatalogoVersionRepository catalogos) {
        this.empresas = empresas;
        this.planes = planes;
        this.suscripciones = suscripciones;
        this.instalaciones = instalaciones;
        this.catalogos = catalogos;
    }

    @PostMapping("/api/v1/licencias/validar")
    public ResponseEntity<Map<String, Object>> validarLicencia(@RequestBody Map<String, Object> body) {
        UUID instalacionId = parseUuid(body.get("instalacionId"));
        InstalacionCliente instalacion = instalacionId != null ? instalaciones.findById(instalacionId).orElse(null) : null;
        SuscripcionCliente suscripcion = instalacion != null
                ? suscripciones.findTopByEmpresaIdOrderByFechaFinDesc(instalacion.getEmpresa().getId()).orElse(null)
                : null;
        PlanComercial plan = suscripcion != null ? suscripcion.getPlan() : planes.findByCodigo("ESTANDAR").orElse(null);
        return ResponseEntity.ok(Map.of(
                "instalacionId", instalacionId != null ? instalacionId : "desconocida",
                "estado", suscripcion != null ? suscripcion.getEstado() : "VALIDO_DEMO",
                "plan", plan != null ? plan.getCodigo() : "ESTANDAR",
                "leaseRenewalAvailable", true,
                "vence", suscripcion != null ? suscripcion.getFechaFin().toString() : OffsetDateTime.now(ZoneOffset.UTC).plusDays(15).toString(),
                "graceUntil", suscripcion != null ? suscripcion.getGraceUntil().toString() : OffsetDateTime.now(ZoneOffset.UTC).plusDays(30).toString(),
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
                "catalogs", catalogos.findByActivoTrueOrderByCatalogoCodigoAsc().stream().map(this::catalog).toList()
        ));
    }

    @GetMapping("/api/v1/catalogs/{code}/versions/{version}")
    public ResponseEntity<Map<String, Object>> catalogVersion(@PathVariable String code, @PathVariable String version) {
        CatalogoVersion catalogo = catalogos.findFirstByCatalogoCodigoAndVersionAndActivoTrue(code, version)
                .orElseThrow(() -> new IllegalArgumentException("Catalogo no encontrado"));
        return ResponseEntity.ok(Map.of(
                "code", code,
                "version", version,
                "hash", catalogo.getSha256(),
                "itemsJson", catalogo.getItemsJson()
        ));
    }

    @GetMapping("/api/v1/configuration/package")
    public ResponseEntity<Map<String, Object>> configurationPackage() {
        PlanComercial plan = planes.findByCodigo("ESTANDAR").orElse(null);
        return ResponseEntity.ok(Map.of(
                "plan", plan != null ? plan.getCodigo() : "ESTANDAR",
                "modules", plan != null ? plan.getModulosJson() : "[]",
                "limits", Map.of(
                        "users", plan != null ? plan.getLimiteUsuarios() : 50,
                        "transactionsMonth", plan != null ? plan.getLimiteTransaccionesMes() : 250000,
                        "kycMonth", plan != null ? plan.getLimiteKycMes() : 10000,
                        "reportsMonth", plan != null ? plan.getLimiteReportesMes() : 1000,
                        "rules", plan != null ? plan.getLimiteReglas() : 40),
                "jobs", Map.of("heartbeatCron", "0 */15 * * * *", "usageSyncCron", "0 5 * * * *", "catalogSyncCron", "0 0 2 * * *")
        ));
    }

    @GetMapping("/api/admin/companies")
    public ResponseEntity<List<Map<String, Object>>> companies() {
        return ResponseEntity.ok(empresas.findAll().stream().map(this::company).toList());
    }

    @GetMapping("/api/admin/installations")
    public ResponseEntity<List<Map<String, Object>>> installations() {
        return ResponseEntity.ok(instalaciones.findAll().stream().map(this::installation).toList());
    }

    @GetMapping("/api/admin/plans")
    public ResponseEntity<List<Map<String, Object>>> plans() {
        return ResponseEntity.ok(planes.findAll().stream().map(this::plan).toList());
    }

    private Map<String, Object> catalog(CatalogoVersion version) {
        String code = version.getCatalogo().getCodigo();
        return Map.of("code", code, "version", version.getVersion(), "sha256", version.getSha256(),
                "downloadUrl", "/api/v1/catalogs/" + code + "/versions/" + version.getVersion());
    }

    private Map<String, Object> company(EmpresaCliente e) {
        return Map.of("id", e.getId(), "codigo", e.getCodigo(), "nombre", e.getNombre(),
                "ruc", e.getRuc() != null ? e.getRuc() : "", "emailContacto", e.getEmailContacto() != null ? e.getEmailContacto() : "",
                "estado", e.getEstado());
    }

    private Map<String, Object> installation(InstalacionCliente i) {
        return Map.of("id", i.getId(), "empresa", i.getEmpresa().getNombre(),
                "identificadorInstalacion", i.getIdentificadorInstalacion(), "estado", i.getEstado(),
                "versionProducto", i.getVersionProducto() != null ? i.getVersionProducto() : "",
                "ultimoHeartbeat", i.getUltimoHeartbeatEn() != null ? i.getUltimoHeartbeatEn().toString() : "");
    }

    private Map<String, Object> plan(PlanComercial p) {
        return Map.of("id", p.getId(), "codigo", p.getCodigo(), "nombre", p.getNombre(),
                "users", p.getLimiteUsuarios(), "transactionsMonth", p.getLimiteTransaccionesMes(),
                "kycMonth", p.getLimiteKycMes(), "reportsMonth", p.getLimiteReportesMes(),
                "rules", p.getLimiteReglas(), "precioAnual", p.getPrecioAnual(), "activo", p.getActivo());
    }

    private UUID parseUuid(Object value) {
        if (value == null) return null;
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
