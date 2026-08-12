INSERT INTO instalacion_cliente (id, empresa_id, identificador_instalacion, fingerprint_hash, version_producto, estado, activada_en, ultimo_heartbeat_en)
VALUES ('00000000-0000-0000-0000-000000009001', '00000000-0000-0000-0000-000000000001', 'SCL-ASUNCION-01', 'sha256:demo-fingerprint-no-productivo', '1.0.0', 'OPERATIVA', now() - interval '10 days', now() - interval '3 minutes')
ON CONFLICT (id) DO UPDATE SET fingerprint_hash = EXCLUDED.fingerprint_hash,
    version_producto = EXCLUDED.version_producto,
    estado = EXCLUDED.estado;
