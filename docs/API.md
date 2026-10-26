# API REST

Base: `http://<host>:3000/api`. JSON. Los tiempos van en milisegundos epoch (`at`, `startedAt`, `recordedAt`).
Errores: `{ "error": "mensaje", "details": {...} }`.

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET   | `/health` | Estado del servidor y de MySQL (503 si la BD no responde) |
| GET   | `/states` | Catálogo de estados (`code`, `displayName`, `priority`, `sprite`) |
| POST  | `/pets` | Crea el monito de un dispositivo (idempotente: 201 si lo crea, 200 si ya existía) |
| GET   | `/pets/:deviceId` | Perfil, progreso de nivel y contadores |
| PATCH | `/pets/:deviceId` | Cambia `name` y/o `personality` |
| POST  | `/pets/:deviceId/events` | Caricia o sacudida: suma contador y XP |
| POST  | `/pets/:deviceId/states` | El reloj cambió de estado: cierra el tramo anterior, abre uno nuevo |
| GET   | `/pets/:deviceId/states?limit=50` | Últimos tramos de estado |
| GET   | `/pets/:deviceId/state-minutes` | Minutos acumulados por estado |
| POST  | `/pets/:deviceId/samples` | Muestra de sensores |

## Ejemplos

`POST /pets`
```json
{ "deviceId": "3f2a9c1e-5b7d-4e8a-9c21-7d4e5f6a8b90", "name": "Monito", "personality": "curioso" }
```
Respuesta:
```json
{
  "id": 1, "deviceId": "3f2a9c1e-…", "name": "Monito", "personality": "curioso",
  "level": 1, "xp": 0, "currentLevelXp": 0, "nextLevelXp": 20,
  "stats": { "petCount": 0, "shakeCount": 0 }
}
```
`POST /pets/:deviceId/events` → `{ "type": "pet", "at": 1792570000000 }` (`type`: `pet` | `shake`)

`POST /pets/:deviceId/states` → `{ "state": "RAIN", "startedAt": 1792570000000 }`

`POST /pets/:deviceId/samples` →
`{ "recordedAt": 1792570000000, "temperatureC": 31.5, "heartRate": 72, "steps": 1200, "pressureHpa": 1012.4 }`
(todos opcionales salvo `recordedAt`)
