# Regula Control Plane Backend

Backend central para administrar licencias, instalaciones, heartbeats, consumo agregado y catálogos versionados de Regula.

## Alcance Del Primer Corte

- API Key obligatoria por `X-API-Key`.
- Endpoints demo de licencia, heartbeat, uso, catálogos y configuración.
- Endpoints admin de empresas, planes e instalaciones.
- Persistencia en memoria para tesis; la siguiente fase debe incorporar PostgreSQL + Flyway.

## Ejecutar

```powershell
mvn spring-boot:run
```

Puerto por defecto: `8090`.

Variables:

```text
REGULA_CONTROL_PLANE_ADMIN_KEY=...
REGULA_CONTROL_PLANE_CLIENT_KEY=...
```

## Endpoints Principales

- `POST /api/v1/licencias/validar`
- `POST /api/v1/telemetry/heartbeat`
- `POST /api/v1/telemetry/usage`
- `GET /api/v1/catalogs/manifest`
- `GET /api/v1/catalogs/{code}/versions/{version}`
- `GET /api/v1/configuration/package`
- `GET /api/admin/companies`
- `GET /api/admin/plans`
- `GET /api/admin/installations`

