package com.regula.controlplane.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ApiEventoControlPlaneService {

    public static final String ATTR_CODIGO_ERROR = "regula.controlplane.codigo_error";
    public static final String ATTR_MENSAJE_ERROR = "regula.controlplane.mensaje_error";
    public static final String ATTR_CATEGORIA_ERROR = "regula.controlplane.categoria_error";
    public static final String ATTR_SKIP_AUTO_EVENT = "regula.controlplane.skip_auto_api_event";

    private final JdbcTemplate jdbcTemplate;

    public ApiEventoControlPlaneService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEntrada(HttpServletRequest request, int status, long duracionMs) {
        if (request == null || request.getRequestURI() == null || !request.getRequestURI().startsWith("/api/")) {
            return;
        }
        String resultado = status >= 400 ? "ERROR" : "EXITOSO";
        registrar(null, null, origen(request.getRequestURI()), "ENTRANTE", servicio(request.getRequestURI()),
                request.getRequestURI(), request.getMethod(), status, attr(request, ATTR_CODIGO_ERROR),
                attr(request, ATTR_MENSAJE_ERROR), resultado, attr(request, ATTR_CATEGORIA_ERROR), duracionMs,
                request.getHeader("X-Correlation-Id"), requestId(request), clientIp(request),
                request.getHeader("User-Agent"), "{}");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCliente(UUID empresaId, UUID instalacionId, String servicio, String endpoint,
                                 String metodo, int status, long duracionMs, String codigoError,
                                 String mensaje, boolean exitoso, String categoriaError, String detalleJson) {
        registrar(empresaId, instalacionId, "CLIENTE_ON_PREMISE", "ENTRANTE", servicio, endpoint, metodo, status,
                codigoError, mensaje, exitoso ? "EXITOSO" : "ERROR", categoriaError, duracionMs, null, null,
                null, null, detalleJson);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorSeguridad(HttpServletRequest request, String codigo, String mensaje) {
        registrarError(request, 401, codigo, mensaje, "SEGURIDAD");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarError(HttpServletRequest request, int status, String codigo, String mensaje, String categoria) {
        registrar(null, null, origen(request != null ? request.getRequestURI() : null), "ENTRANTE",
                servicio(request != null ? request.getRequestURI() : null),
                request != null ? request.getRequestURI() : null,
                request != null ? request.getMethod() : null, status, codigo, mensaje, "ERROR", categoria,
                null, request != null ? request.getHeader("X-Correlation-Id") : null,
                request != null ? requestId(request) : null, request != null ? clientIp(request) : null,
                request != null ? request.getHeader("User-Agent") : null, "{}");
    }

    private void registrar(UUID empresaId, UUID instalacionId, String origen, String direccion, String servicio,
                           String endpoint, String metodo, Integer status, String codigoError, String mensaje,
                           String resultado, String categoriaError, Long duracionMs, String correlationId,
                           String requestId, String ip, String userAgent, String detalleJson) {
        try {
            jdbcTemplate.update("""
                    insert into api_evento_control_plane (
                        empresa_id, instalacion_id, origen, direccion, servicio, endpoint, metodo_http,
                        status_http, codigo_error, mensaje, resultado, categoria_error, duracion_ms,
                        correlation_id, request_id, ip_origen, user_agent, detalle_json
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, cast(? as jsonb))
                    """, empresaId, instalacionId, origen, direccion, servicio, endpoint, metodo, status, codigoError,
                    mensaje, resultado, categoriaError, duracionMs, correlationId, requestId, ip, userAgent,
                    detalleJson != null ? detalleJson : "{}");
        } catch (RuntimeException ignored) {
            // La telemetria no debe bloquear licenciamiento ni administracion central.
        }
    }

    private String attr(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value != null ? String.valueOf(value) : null;
    }

    private String origen(String path) {
        return path != null && path.startsWith("/api/admin") ? "ADMIN_CONTROL_PLANE" : "CLIENTE_ON_PREMISE";
    }

    private String servicio(String path) {
        if (path == null) return "CONTROL_PLANE";
        if (path.startsWith("/api/admin")) return "ADMIN";
        if (path.contains("/licencias")) return "LICENCIAS";
        if (path.contains("/telemetry/heartbeat")) return "HEARTBEAT";
        if (path.contains("/telemetry/usage")) return "CONSUMO";
        if (path.contains("/catalogs")) return "CATALOGOS";
        if (path.contains("/configuration")) return "CONFIGURACION";
        return "CONTROL_PLANE";
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId != null && !requestId.isBlank() ? requestId : request.getHeader("X-Correlation-Id");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
