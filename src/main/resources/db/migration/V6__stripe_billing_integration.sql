ALTER TABLE plan_comercial
    ADD COLUMN IF NOT EXISTS stripe_product_id VARCHAR(120),
    ADD COLUMN IF NOT EXISTS stripe_price_id VARCHAR(120);

ALTER TABLE pago_cliente
    ADD COLUMN IF NOT EXISTS proveedor_pago VARCHAR(40) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN IF NOT EXISTS stripe_checkout_session_id VARCHAR(180),
    ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(180),
    ADD COLUMN IF NOT EXISTS stripe_subscription_id VARCHAR(180),
    ADD COLUMN IF NOT EXISTS stripe_invoice_id VARCHAR(180),
    ADD COLUMN IF NOT EXISTS stripe_payment_intent_id VARCHAR(180),
    ADD COLUMN IF NOT EXISTS checkout_url TEXT,
    ADD COLUMN IF NOT EXISTS fecha_vencimiento TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS detalle_json JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE UNIQUE INDEX IF NOT EXISTS ux_pago_cliente_stripe_session
    ON pago_cliente (stripe_checkout_session_id)
    WHERE stripe_checkout_session_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_pago_cliente_empresa_estado
    ON pago_cliente (empresa_id, estado, fecha_hora_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_pago_cliente_stripe_subscription
    ON pago_cliente (stripe_subscription_id)
    WHERE stripe_subscription_id IS NOT NULL;

COMMENT ON COLUMN plan_comercial.stripe_product_id IS 'ID del producto Stripe asociado al plan comercial cuando existe catalogo configurado en Stripe.';
COMMENT ON COLUMN plan_comercial.stripe_price_id IS 'ID del price recurrente Stripe. Si no se configura, el Control Plane puede crear Checkout con price_data para pruebas.';
COMMENT ON COLUMN pago_cliente.proveedor_pago IS 'Proveedor que procesa el pago: MANUAL, STRIPE u otro proveedor futuro.';
COMMENT ON COLUMN pago_cliente.stripe_checkout_session_id IS 'ID de Checkout Session usado para reconciliar checkout.session.completed.';
COMMENT ON COLUMN pago_cliente.checkout_url IS 'URL efimera de Stripe Checkout entregada al administrador de empresa.';
COMMENT ON COLUMN pago_cliente.detalle_json IS 'Detalle tecnico sanitizado del intento y eventos de pago. No guardar datos completos de tarjeta.';
