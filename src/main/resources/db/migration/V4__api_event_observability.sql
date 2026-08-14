CREATE TABLE IF NOT EXISTS api_evento_control_plane (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id UUID REFERENCES empresa_cliente(id) ON DELETE SET NULL ON UPDATE CASCADE,
    instalacion_id UUID REFERENCES instalacion_cliente(id) ON DELETE SET NULL ON UPDATE CASCADE,
    origen VARCHAR(30) NOT NULL,
    direccion VARCHAR(20) NOT NULL,
    servicio VARCHAR(100) NOT NULL,
    endpoint VARCHAR(250),
    metodo_http VARCHAR(12),
    status_http INTEGER,
    codigo_error VARCHAR(100),
    mensaje TEXT,
    resultado VARCHAR(20) NOT NULL,
    categoria_error VARCHAR(80),
    duracion_ms BIGINT,
    correlation_id VARCHAR(120),
    request_id VARCHAR(120),
    ip_origen VARCHAR(100),
    user_agent TEXT,
    detalle_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_api_evento_cp_fecha ON api_evento_control_plane (fecha_evento DESC);
CREATE INDEX IF NOT EXISTS idx_api_evento_cp_empresa_fecha ON api_evento_control_plane (empresa_id, fecha_evento DESC);
CREATE INDEX IF NOT EXISTS idx_api_evento_cp_resultado ON api_evento_control_plane (resultado, fecha_evento DESC);
CREATE INDEX IF NOT EXISTS idx_api_evento_cp_servicio ON api_evento_control_plane (servicio, endpoint, fecha_evento DESC);
CREATE INDEX IF NOT EXISTS idx_api_evento_cp_instalacion ON api_evento_control_plane (instalacion_id, fecha_evento DESC);

COMMENT ON TABLE api_evento_control_plane IS 'Telemetria tecnica del Control Plane central: requests admin, requests de clientes on-premise, errores, latencia y trafico por empresa.';
COMMENT ON COLUMN api_evento_control_plane.empresa_id IS 'Empresa cliente asociada cuando el evento proviene de una instalacion o suscripcion identificable.';
COMMENT ON COLUMN api_evento_control_plane.resultado IS 'EXITOSO o ERROR para alimentar tableros y alertas operativas.';
