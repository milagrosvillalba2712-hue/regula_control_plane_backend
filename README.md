# Regula Control Plane Backend

Backend central para administrar licencias, instalaciones, heartbeats, consumo agregado y catálogos versionados de Regula.

## Alcance Del Corte Actual

- API Key obligatoria por `X-API-Key`.
- Endpoints de licencia, heartbeat, uso, catálogos y configuración.
- Endpoints admin de empresas, planes e instalaciones.
- Emisión de lease firmado RS256 y publicación JWKS.
- Persistencia de heartbeat y consumo agregado sanitizado.
- Persistencia PostgreSQL con Flyway.
- Seed demo de `Financiera Santa Clara`.

## Ejecutar

```powershell
mvn spring-boot:run
```

Puerto por defecto: `8090`.

Variables:

```text
DB_HOST=localhost
DB_PORT=5433
DB_NAME=regula_control_plane
DB_USER=regula_owner
DB_PASSWORD=regula
REGULA_CONTROL_PLANE_ADMIN_KEY=...
REGULA_CONTROL_PLANE_CLIENT_KEY=...
```

Para conectar el cliente on-premise (`sistema_antifraude_backend`) a este Control Plane:

```text
APP_LICENSES_CONTROL_PLANE_URL=http://localhost:8090
APP_LICENSES_CONTROL_PLANE_API_KEY=<REGULA_CONTROL_PLANE_CLIENT_KEY>
```

## Endpoints Principales

- `POST /api/v1/licencias/validar`
- `GET /api/v1/licencias/jwks`
- `POST /api/v1/telemetry/heartbeat`
- `POST /api/v1/telemetry/usage`
- `GET /api/v1/catalogs/manifest`
- `GET /api/v1/catalogs/{code}/versions/{version}`
- `GET /api/v1/configuration/package`
- `GET /api/admin/companies`
- `GET /api/admin/plans`
- `GET /api/admin/installations`

## Siguiente Fase

- Registro CRUD de empresas, contratos, pagos e instalaciones.
- Revocación de instalaciones.
- Rotación formal de claves RSA/ECC.
- Archivo offline `.lic`.
- Auditoría central completa.
