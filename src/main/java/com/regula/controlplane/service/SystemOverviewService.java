package com.regula.controlplane.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SystemOverviewService {

    private final JdbcTemplate jdbcTemplate;

    public SystemOverviewService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> overview() {
        Map<String, Object> database = databaseMetrics();
        List<Map<String, Object>> companies = companyUsage();
        return mapOf(
                "database", database,
                "apiLatency", apiLatency(),
                "activeConnections", database.get("activeConnections"),
                "systemUptime", uptime(),
                "trafficTrend24h", trafficTrend24h(),
                "errorTelemetry", errorHistory(),
                "companies", companies,
                "generatedAt", OffsetDateTime.now()
        );
    }

    private Map<String, Object> databaseMetrics() {
        Integer activeConnections = queryInt("select count(*) from pg_stat_activity where datname = current_database()", 0);
        Integer maxConnections = queryInt("select current_setting('max_connections')::int", 100);
        double load = maxConnections != null && maxConnections > 0
                ? Math.round((activeConnections * 10000.0) / maxConnections) / 100.0
                : 0.0;
        return mapOf(
                "label", "Presion Por Conexiones",
                "loadPercent", load,
                "activeConnections", activeConnections,
                "maxConnections", maxConnections,
                "description", "Presion aproximada calculada sobre conexiones activas de PostgreSQL."
        );
    }

    private Map<String, Object> apiLatency() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                select
                  coalesce(round(avg(duracion_ms))::int, 0) as avg_ms,
                  coalesce(round(percentile_cont(0.95) within group (order by duracion_ms))::int, 0) as p95_ms,
                  count(*)::int as total
                from api_evento_control_plane
                where fecha_evento >= now() - interval '24 hours'
                  and duracion_ms is not null
                """);
        return mapOf(
                "avgMs", row.get("avg_ms"),
                "p95Ms", row.get("p95_ms"),
                "totalSamples", row.get("total"),
                "scope", "Latencia real por requests registrados en api_evento_control_plane durante las ultimas 24 horas."
        );
    }

    private Map<String, Object> uptime() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptimeMs);
        return mapOf(
                "uptimeMs", uptimeMs,
                "uptimeSeconds", duration.toSeconds(),
                "display", displayDuration(duration),
                "description", "Tiempo desde el ultimo arranque del Control Plane. Informa estabilidad y reinicios inesperados."
        );
    }

    private List<Map<String, Object>> trafficTrend24h() {
        return jdbcTemplate.queryForList("""
                with hours as (
                  select generate_series(
                    date_trunc('hour', now()) - interval '23 hours',
                    date_trunc('hour', now()),
                    interval '1 hour'
                  ) as bucket
                ),
                heartbeats as (
                  select date_trunc('hour', fecha_evento) as bucket, count(*)::int as total
                  from heartbeat_instalacion
                  where fecha_evento >= now() - interval '24 hours'
                  group by 1
                ),
                usage as (
                  select date_trunc('hour', fecha_evento) as bucket, count(*)::int as total
                  from consumo_reportado
                  where fecha_evento >= now() - interval '24 hours'
                  group by 1
                ),
                audit as (
                  select date_trunc('hour', fecha_evento) as bucket, count(*)::int as total
                  from auditoria_control_plane
                  where fecha_evento >= now() - interval '24 hours'
                  group by 1
                ),
                api as (
                  select date_trunc('hour', fecha_evento) as bucket,
                         count(*)::int as total,
                         count(*) filter (where origen = 'ADMIN_CONTROL_PLANE')::int as admin,
                         count(*) filter (where origen = 'CLIENTE_ON_PREMISE')::int as cliente,
                         count(*) filter (where resultado = 'ERROR')::int as errores
                  from api_evento_control_plane
                  where fecha_evento >= now() - interval '24 hours'
                  group by 1
                )
                select
                  h.bucket,
                  coalesce(hb.total, 0) as heartbeats,
                  coalesce(u.total, 0) as consumo_reportado,
                  coalesce(a.total, 0) as auditoria,
                  coalesce(api.admin, 0) as api_admin,
                  coalesce(api.cliente, 0) as api_cliente,
                  coalesce(api.errores, 0) as api_errores,
                  coalesce(api.total, 0) as api_total,
                  coalesce(hb.total, 0) + coalesce(u.total, 0) + coalesce(a.total, 0) + coalesce(api.total, 0) as total
                from hours h
                left join heartbeats hb on hb.bucket = h.bucket
                left join usage u on u.bucket = h.bucket
                left join audit a on a.bucket = h.bucket
                left join api on api.bucket = h.bucket
                order by h.bucket
                """);
    }

    private List<Map<String, Object>> errorHistory() {
        return jdbcTemplate.queryForList("""
                select
                  coalesce(codigo_error, 'HTTP_' || status_http, 'CONTROL_PLANE_ERROR') as codigo,
                  coalesce(mensaje, categoria_error, 'Error tecnico de API') as mensaje,
                  servicio as origen,
                  coalesce(correlation_id, request_id) as referencia,
                  ip_origen,
                  status_http,
                  fecha_evento as fecha
                from api_evento_control_plane
                where resultado = 'ERROR'
                order by fecha_evento desc
                limit 80
                """);
    }

    private List<Map<String, Object>> companyUsage() {
        return jdbcTemplate.queryForList("""
                select
                  e.id as empresa_id,
                  e.codigo,
                  e.nombre,
                  e.estado,
                  coalesce(sum(c.transacciones), 0) as transacciones,
                  coalesce(sum(c.consultas_kyc), 0) as consultas_kyc,
                  coalesce(sum(c.reportes), 0) as reportes,
                  max(i.ultimo_heartbeat_en) as ultimo_contacto
                from empresa_cliente e
                left join instalacion_cliente i on i.empresa_id = e.id
                left join consumo_reportado c on c.instalacion_id = i.id
                group by e.id, e.codigo, e.nombre, e.estado
                order by e.nombre
                """);
    }

    private Integer queryInt(String sql, int fallback) {
        try {
            Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
            return value != null ? value : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private String displayDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }
}
