# API de peso y compatibilidad offline

## Endpoints

### `GET /api/peso`

Devuelve el historico del usuario autenticado ordenado por `fechaRegistro` descendente.

Respuesta ejemplo:

```json
[
  {
    "id": "12",
    "userId": 1,
    "clientId": "peso-2026-04-25",
    "peso": 82.4,
    "fechaRegistro": "2026-04-25",
    "createdAt": "2026-04-25T08:00:00",
    "updatedAt": "2026-04-25T12:15:00",
    "version": 3
  }
]
```

### `PUT /api/peso/hoy`

Crea o actualiza el peso del dia actual para el usuario autenticado. Si el usuario ya tiene peso en el dia actual, el backend actualiza el registro existente.

Request ejemplo:

```json
{
  "peso": 82.4,
  "clientId": "peso-2026-04-25",
  "version": 2
}
```

Respuesta ejemplo:

```json
{
  "id": "12",
  "userId": 1,
  "clientId": "peso-2026-04-25",
  "peso": 82.4,
  "fechaRegistro": "2026-04-25",
  "createdAt": "2026-04-25T08:00:00",
  "updatedAt": "2026-04-25T12:15:00",
  "version": 3
}
```

### `POST /api/peso`

Endpoint adicional para resincronizacion o carga diferida. Permite guardar un registro para una fecha concreta con payload idempotente por `clientId` o por combinacion `usuario + fechaRegistro`.

Request ejemplo:

```json
{
  "peso": 81.9,
  "fechaRegistro": "2026-04-24",
  "clientId": "peso-2026-04-24",
  "version": 1
}
```

## Contrato de errores

Todos los errores de negocio y 5xx responden en JSON consistente:

```json
{
  "error": "Peticion no valida",
  "mensaje": "peso: El peso debe ser mayor que 0",
  "statusCode": 400,
  "timestamp": "2026-04-25T12:30:00"
}
```

## Compatibilidad frontend

- `GET /api/auth/me` sigue devolviendo `id`, `nombre`, `email`, `username`, `rol`, `activo`, `createdAt`, `updatedAt`.
- Ejercicios, sesiones y entrenamientos conservan sus campos actuales y ahora exponen ademas `clientId`, `createdAt`, `updatedAt` y `version`.
- Las respuestas mantienen fechas en formato ISO para cacheo local y futuras estrategias de sincronizacion.
