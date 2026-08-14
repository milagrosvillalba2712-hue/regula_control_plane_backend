CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE empresa_cliente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(60) NOT NULL UNIQUE,
    nombre VARCHAR(180) NOT NULL,
    ruc VARCHAR(40),
    email_contacto VARCHAR(180),
    estado VARCHAR(30) NOT NULL,
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_hora_modificacion TIMESTAMPTZ
);

CREATE TABLE plan_comercial (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    limite_usuarios INTEGER NOT NULL,
    limite_transacciones_mes BIGINT NOT NULL,
    limite_kyc_mes BIGINT NOT NULL,
    limite_reportes_mes BIGINT NOT NULL,
    limite_reglas INTEGER NOT NULL,
    precio_anual NUMERIC(18,2) NOT NULL,
    modulos_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_hora_modificacion TIMESTAMPTZ
);

CREATE TABLE suscripcion_cliente (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES plan_comercial(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    grace_until DATE NOT NULL,
    estado VARCHAR(40) NOT NULL,
    renovacion_automatica BOOLEAN NOT NULL DEFAULT true,
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_hora_modificacion TIMESTAMPTZ
);

CREATE TABLE pago_cliente (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    suscripcion_id BIGINT REFERENCES suscripcion_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    codigo VARCHAR(80) NOT NULL UNIQUE,
    monto NUMERIC(18,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    fecha_pago TIMESTAMPTZ,
    estado VARCHAR(30) NOT NULL,
    metodo_pago VARCHAR(60),
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_hora_modificacion TIMESTAMPTZ
);

CREATE TABLE instalacion_cliente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    identificador_instalacion VARCHAR(120) NOT NULL UNIQUE,
    fingerprint_hash VARCHAR(128) NOT NULL,
    version_producto VARCHAR(40),
    estado VARCHAR(40) NOT NULL,
    activada_en TIMESTAMPTZ,
    ultimo_heartbeat_en TIMESTAMPTZ,
    clon_detectado BOOLEAN NOT NULL DEFAULT false,
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_hora_modificacion TIMESTAMPTZ
);

CREATE TABLE lease_emitido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instalacion_id UUID NOT NULL REFERENCES instalacion_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    suscripcion_id BIGINT NOT NULL REFERENCES suscripcion_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    estado VARCHAR(30) NOT NULL,
    plan_codigo VARCHAR(40) NOT NULL,
    emitido_en TIMESTAMPTZ NOT NULL,
    vence_en TIMESTAMPTZ NOT NULL,
    grace_until TIMESTAMPTZ NOT NULL,
    kid VARCHAR(120) NOT NULL,
    nonce UUID NOT NULL,
    lease_payload JSONB NOT NULL,
    firma VARCHAR(500) NOT NULL,
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE heartbeat_instalacion (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    instalacion_id UUID NOT NULL REFERENCES instalacion_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    estado_reportado VARCHAR(40) NOT NULL,
    version_producto VARCHAR(40),
    ip_origen VARCHAR(100),
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE consumo_reportado (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    instalacion_id UUID NOT NULL REFERENCES instalacion_cliente(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    periodo VARCHAR(20) NOT NULL,
    usuarios_activos INTEGER NOT NULL DEFAULT 0,
    transacciones BIGINT NOT NULL DEFAULT 0,
    consultas_kyc BIGINT NOT NULL DEFAULT 0,
    reportes BIGINT NOT NULL DEFAULT 0,
    reglas INTEGER NOT NULL DEFAULT 0,
    payload_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalogo_publicado (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL UNIQUE,
    nombre VARCHAR(160) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_hora_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalogo_version (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    catalogo_id BIGINT NOT NULL REFERENCES catalogo_publicado(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    version VARCHAR(40) NOT NULL,
    sha256 VARCHAR(128) NOT NULL,
    items_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    publicado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    activo BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (catalogo_id, version)
);

CREATE TABLE auditoria_control_plane (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(100),
    entidad_id VARCHAR(120),
    descripcion TEXT,
    ip_origen VARCHAR(100),
    fecha_evento TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_suscripcion_empresa_estado ON suscripcion_cliente (empresa_id, estado);
CREATE INDEX idx_instalacion_empresa_estado ON instalacion_cliente (empresa_id, estado);
CREATE INDEX idx_heartbeat_instalacion_fecha ON heartbeat_instalacion (instalacion_id, fecha_evento DESC);
CREATE INDEX idx_consumo_instalacion_periodo ON consumo_reportado (instalacion_id, periodo);
CREATE INDEX idx_catalogo_version_activa ON catalogo_version (catalogo_id, activo);

