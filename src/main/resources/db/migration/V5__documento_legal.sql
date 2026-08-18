CREATE TABLE IF NOT EXISTS documento_legal (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo                  VARCHAR(20) NOT NULL,
    version               INTEGER NOT NULL,
    titulo                VARCHAR(255) NOT NULL,
    contenido             TEXT NOT NULL,
    url_documento         VARCHAR(500),
    activo                BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion        TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_publicacion     TIMESTAMPTZ,
    fecha_hora_creacion   TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_hora_modificacion TIMESTAMPTZ,
    CONSTRAINT ck_documento_legal_tipo CHECK (tipo IN ('TERMINOS', 'POLITICA_PRIVACIDAD')),
    CONSTRAINT uk_documento_legal_version UNIQUE (tipo, version)
);

CREATE INDEX IF NOT EXISTS idx_documento_legal_tipo ON documento_legal (tipo, version, activo);

COMMENT ON TABLE documento_legal IS 'Documentos legales versionados (Términos y Condiciones / Política de Privacidad) gestionados por Regula.';
COMMENT ON COLUMN documento_legal.tipo IS 'TERMINOS para Términos y Condiciones; POLITICA_PRIVACIDAD para Política de Privacidad.';
COMMENT ON COLUMN documento_legal.version IS 'Número de versión incremental por tipo.';
