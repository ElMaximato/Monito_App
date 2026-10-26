# Arquitectura

## Visión general

```
 ┌────────────── RELOJ (wear) ──────────────┐        ┌──────────── CELULAR (mobile) ────────────┐        ┌── PC ──┐
 │ SensorHub ─┐                             │        │ WatchListenerService                     │  HTTP  │ Express│
 │ HeartRate ─┼─► StateResolver ─► PetScreen│─ ────► │   └► WatchEventHandler ─► PetRepository ─┼───────►│  API   │──► MySQL
 │ Toque ─────┘        │                    │ Data   │ WeatherSync ─► Open-Meteo                │        └────────┘
 │ PhoneMessenger ◄────┘ (eventos, estado)  │ Layer  │ HubScreen / ProfileScreen                │
 │ PhoneListenerService ◄───────────────────┼◄────── │ WatchMessenger (clima, perfil)           │
 └──────────────────────────────────────────┘        └──────────────────────────────────────────┘
```

- **El reloj decide el estado** al instante (la sacudida no espera al celular).
- **El celular es el puente**: recibe lo del reloj, lo envía al backend y es el único que consulta el clima.
- **El backend es la fuente de verdad** de perfil, XP, nivel y estadísticas (MySQL).
  *(Cambio respecto al primer documento: en lugar de Room local, ahora hay backend MySQL.)*

## Módulos Android

| Módulo   | Responsabilidad | Carpetas principales |
|----------|-----------------|----------------------|
| `shared` | Lo que reloj y móvil deben entender igual | `state/PetState`, `protocol/*Message` |
| `mobile` | UI del celular y puente al backend | `presentation/` (UI), `domain/` (modelos, casos de uso), `data/` (Retrofit, repositorios), `watch/` (Data Layer), `sync/`, `di/` |
| `wear`   | Cuerpo del monito | `domain/rules/StateResolver`, `sensors/`, `health/`, `communication/`, `presentation/` |

Reglas de dependencia: `presentation → domain ← data`. `domain` no conoce Android ni Retrofit.

## Mensajes reloj ↔ móvil (Wearable Data Layer, MessageClient)

| Ruta            | Sentido        | Contenido                                   | Cuándo                      |
|-----------------|----------------|---------------------------------------------|-----------------------------|
| `/event/shake`  | reloj → móvil  | `at`                                        | al sacudir                  |
| `/event/pet`    | reloj → móvil  | `at`                                        | al tocar la pantalla        |
| `/sample`       | reloj → móvil  | temperatura, pulso, pasos, presión, hora    | cada minuto                 |
| `/state`        | reloj → móvil  | estado y hora de inicio                     | al cambiar de estado        |
| `/weather`      | móvil → reloj  | `isRaining`, `checkedAt`                    | cada 15 min                 |
| `/profile`      | móvil → reloj  | nombre, nivel, personalidad                 | al cargar/cambiar el perfil |

Si el móvil no está conectado, el reloj guarda hasta 100 mensajes en memoria y los reenvía después
(pendiente: persistirlos en disco).

## Estados y prioridad

El reloj reúne los estados que aplican y gana el de menor número (`PetState.priority`).
Los códigos y prioridades deben coincidir con `database/seed.sql`.

| Prioridad | Estados |
|-----------|---------|
| 1 | SCARED (sacudida, 4 s) |
| 2 | PET (caricia, 2 s) |
| 3 | CLIMB, RUN, WALK |
| 4 | EXCITED (pulso alto), CALM (pulso bajo y quieto) |
| 5 | RAIN, HOT, COLD |
| 6 | NIGHT |
| 7 | BORED |
| 8 | IDLE |

Umbrales provisionales en `wear/.../domain/rules/Thresholds.kt` (calor ≥ 30 °C, frío ≤ 15 °C,
pulso alto ≥ 110, bajo ≤ 65, noche 21:00–06:00, aburrido a los 300 s sin moverse).

## Base de datos

`pet_state` (catálogo) · `pet_profile` · `pet_stats` · `state_log` · `sensor_sample` ·
vista `v_pet_state_minutes`. Un monito por teléfono, identificado por un `device_id` anónimo (sin login).

## Decisiones y pendientes

- Ambas apps usan el mismo `applicationId` (`com.monito.app`): requisito de la Data Layer.
- XP y nivel (backend, `utils/xp.js`): reglas provisionales; 2 XP por caricia, 1 por sacudida,
  1 por cada 10 min en un estado.
- Clima: necesita una ubicación. Hoy es fija (`gradle.properties`); decidir si se pide ubicación aproximada.
- Sprites faltantes: trepar, cara de cansancio al correr, accesorios (bufanda, abanico, paraguas, botas), baile.
