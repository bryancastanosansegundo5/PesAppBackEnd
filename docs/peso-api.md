# API de peso y compatibilidad offline

## Endpoints

### `GET /api/peso`

Devuelve el historico completo del usuario autenticado ordenado por `fecha` descendente.

Respuesta ejemplo:

```json
[
  {
    "id": "12",
    "userId": 1,
    "clientId": "peso-2026-04-25",
    "peso": 82.4,
    "fechaRegistro": "2026-04-25",
    "horaRegistro": "08:00",
    "horaManual": false,
    "comentario": "Peso en ayunas tras descansar bien",
    "fecha": "2026-04-25T08:00:00",
    "createdAt": "2026-04-25T08:00:00",
    "updatedAt": "2026-04-25T12:15:00",
    "version": 3
  }
]
```

### `PUT /api/peso/hoy`

Crea o actualiza un peso del dia actual para el usuario autenticado. El backend solo actualiza un registro existente cuando coincide el mismo `clientId`; varios pesos en el mismo dia son validos si cada medicion usa un `clientId` distinto.

Request ejemplo:

```json
{
  "peso": 82.4,
  "horaRegistro": "12:15",
  "horaManual": false,
  "clientId": "peso-2026-04-25",
  "comentario": "Peso en ayunas tras descansar bien",
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
  "horaRegistro": "12:15",
  "horaManual": false,
  "comentario": "Peso en ayunas tras descansar bien",
  "fecha": "2026-04-25T12:15:00",
  "createdAt": "2026-04-25T08:00:00",
  "updatedAt": "2026-04-25T12:15:00",
  "version": 3
}
```

### `POST /api/peso`

Endpoint adicional para resincronizacion o carga diferida. Permite guardar un registro para una fecha concreta con payload idempotente por `clientId`. Varios pesos en la misma `fechaRegistro` son validos si usan `clientId` distintos.

Request ejemplo:

```json
{
  "peso": 81.9,
  "fechaRegistro": "2026-04-24",
  "horaRegistro": "21:40",
  "horaManual": true,
  "clientId": "peso-2026-04-24",
  "comentario": "Peso antes del desayuno",
  "version": 1
}
```

Respuesta ejemplo:

```json
{
  "id": "11",
  "userId": 1,
  "clientId": "peso-2026-04-24",
  "peso": 81.9,
  "fechaRegistro": "2026-04-24",
  "horaRegistro": "21:40",
  "horaManual": true,
  "comentario": "Peso antes del desayuno",
  "fecha": "2026-04-24T21:40:00",
  "createdAt": "2026-04-25T09:10:00",
  "updatedAt": "2026-04-25T09:10:00",
  "version": 1
}
```

### `DELETE /api/peso/{id}`

Elimina de forma definitiva un registro de peso. Un usuario solo puede borrar sus propios registros; un admin puede borrar cualquier registro.

Respuestas:

- `204 No Content` si el registro se elimina correctamente
- `404 Not Found` si no existe un peso con ese `id`
- `403 Forbidden` si el usuario autenticado no tiene permisos sobre ese registro

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
- Los registros antiguos sin hora se migran con `horaManual = false`, `horaRegistro` derivada de `createdAt` y `fecha` reconstruida desde `fechaRegistro + horaRegistro`.
- Para conservar varias mediciones del mismo dia, cada alta debe enviarse con un `clientId` unico por registro.
- Las respuestas mantienen fechas en formato ISO para cacheo local y futuras estrategias de sincronizacion.
