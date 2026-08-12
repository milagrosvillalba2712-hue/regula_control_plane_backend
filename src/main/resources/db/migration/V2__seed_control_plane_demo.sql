INSERT INTO empresa_cliente (id, codigo, nombre, ruc, email_contacto, estado)
VALUES ('00000000-0000-0000-0000-000000000001', 'FIN-SANTA-CLARA', 'Financiera Santa Clara', '80012345-6', 'admin@santaclara.example.invalid', 'ACTIVA')
ON CONFLICT (id) DO NOTHING;

INSERT INTO plan_comercial (codigo, nombre, descripcion, limite_usuarios, limite_transacciones_mes, limite_kyc_mes, limite_reportes_mes, limite_reglas, precio_anual, modulos_json)
VALUES
('BASICO', 'Basico', 'Plan inicial para cooperativas pequenas', 10, 50000, 1000, 100, 10, 12000000, '["TRANSACCIONES","ALERTAS"]'),
('ESTANDAR', 'Estandar', 'Plan recomendado para entidades medianas', 50, 250000, 10000, 1000, 40, 48000000, '["TRANSACCIONES","ALERTAS","KYC","REGLAS","REPORTES"]'),
('PREMIUM', 'Premium', 'Plan completo con limites ampliados', 200, 1000000, 50000, 5000, 100, 120000000, '["TRANSACCIONES","ALERTAS","KYC","REGLAS","REPORTES","ROS"]')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO suscripcion_cliente (empresa_id, plan_id, fecha_inicio, fecha_fin, grace_until, estado, renovacion_automatica)
SELECT '00000000-0000-0000-0000-000000000001', p.id, DATE '2026-01-01', DATE '2026-12-31', DATE '2027-01-15', 'ACTIVA', true
FROM plan_comercial p
WHERE p.codigo = 'ESTANDAR'
ON CONFLICT DO NOTHING;

INSERT INTO pago_cliente (empresa_id, suscripcion_id, codigo, monto, moneda, fecha_pago, estado, metodo_pago)
SELECT '00000000-0000-0000-0000-000000000001', s.id, 'PAY-DEMO-2026-001', 48000000, 'PYG', now() - interval '15 days', 'CONFIRMADO', 'TRANSFERENCIA'
FROM suscripcion_cliente s
WHERE s.empresa_id = '00000000-0000-0000-0000-000000000001'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO instalacion_cliente (id, empresa_id, identificador_instalacion, fingerprint_hash, version_producto, estado, activada_en, ultimo_heartbeat_en)
VALUES ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001', 'FIN-SANTA-CLARA-ONPREM-01', repeat('a', 64), '2026.08-demo', 'OPERATIVA', now() - interval '10 days', now() - interval '3 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO lease_emitido (instalacion_id, suscripcion_id, estado, plan_codigo, emitido_en, vence_en, grace_until, kid, nonce, lease_payload, firma)
SELECT '00000000-0000-0000-0000-000000000101', s.id, 'VIGENTE', 'ESTANDAR', now() - interval '1 day', now() + interval '15 days', now() + interval '30 days', 'demo-rsa-2026-01', gen_random_uuid(),
       '{"iss":"regula-control-plane","plan":"ESTANDAR","demo":true}'::jsonb,
       'firma-demo-no-criptografica'
FROM suscripcion_cliente s
WHERE s.empresa_id = '00000000-0000-0000-0000-000000000001'
ON CONFLICT DO NOTHING;

INSERT INTO catalogo_publicado (codigo, nombre, descripcion)
VALUES
('PAISES_ISO', 'Paises ISO', 'Catalogo ISO de paises'),
('MONEDAS_ISO', 'Monedas ISO', 'Catalogo ISO de monedas'),
('PAISES_RIESGO', 'Paises De Riesgo', 'Paises con monitoreo incrementado'),
('LISTAS_RIESGO_DEMO', 'Listas De Riesgo Demo', 'Sujetos sinteticos para pruebas')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO catalogo_version (catalogo_id, version, sha256, items_json)
SELECT id, '2026.08.01', 'sha256-demo-' || lower(codigo),
       jsonb_build_array(jsonb_build_object('codigo', codigo || '_001', 'estado', 'ACTIVO'))
FROM catalogo_publicado
ON CONFLICT (catalogo_id, version) DO NOTHING;

