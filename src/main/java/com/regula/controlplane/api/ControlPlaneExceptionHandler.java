package com.regula.controlplane.api;

import com.regula.controlplane.service.ApiEventoControlPlaneService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ControlPlaneExceptionHandler {

    private final JdbcTemplate jdbcTemplate;

    public ControlPlaneExceptionHandler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handle(Exception ex, HttpServletRequest request) {
        if (request != null) {
            request.setAttribute(ApiEventoControlPlaneService.ATTR_CODIGO_ERROR, "CONTROL_PLANE_ERROR");
            request.setAttribute(ApiEventoControlPlaneService.ATTR_CATEGORIA_ERROR, "SISTEMA");
            request.setAttribute(ApiEventoControlPlaneService.ATTR_MENSAJE_ERROR, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        audit("CONTROL_PLANE_ERROR", request != null ? request.getRequestURI() : null,
                ex.getClass().getSimpleName() + ": " + ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "codigo", "CONTROL_PLANE_ERROR",
                "mensaje", "Error interno del Control Plane",
                "path", request != null ? request.getRequestURI() : ""
        ));
    }

    public void audit(String action, String entityId, String description, HttpServletRequest request) {
        try {
            jdbcTemplate.update("""
                    insert into auditoria_control_plane (accion, entidad, entidad_id, descripcion, ip_origen)
                    values (?, ?, ?, ?, ?)
                    """, action, "api_endpoint", entityId, description, clientIp(request));
        } catch (RuntimeException ignored) {
            // No se propaga: la auditoria de error nunca debe ocultar el error original.
        }
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
