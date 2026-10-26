# Plan paso a paso

Qué trae esta estructura y qué falta, siguiendo las fases del documento de MVP.

| Fase | Contenido | Estado |
|------|-----------|--------|
| 0 · Base | Proyecto móvil + Wear OS, sprites cargados, mensaje reloj → móvil | Escrito. Falta: sincronizar en Android Studio y emparejar emulador con el S23 |
| 1 · Estados | `StateResolver`, panel de simulación (pulsación larga) | Escrito, con pruebas unitarias en `wear/src/test` |
| 2 · Sensores | Sacudida, movimiento, temperatura, barómetro, pulso, toque | Escrito. Falta calibrar umbrales en el emulador |
| 3 · Móvil | Backend + MySQL, perfil con XP, hub con el fondo | Backend probado. Falta probar de punta a punta con el reloj |
| 4 · Clima y demo | Lluvia por Open-Meteo, animación del hub, sprites faltantes | Clima escrito (ubicación pendiente). Animación y sprites: pendientes |

## Siguiente paso recomendado

1. Abrir `android/` en Android Studio y lograr el *Sync* y que ambas apps compilen.
2. Levantar backend + MySQL y ver al monito creado en la tabla `pet_profile`.
3. Correr el reloj en el emulador, abrir el panel de simulación y confirmar que cambian los sprites.
4. Emparejar con el S23 y comprobar que una caricia sube el XP en el perfil.

## Pendientes conocidos

- Persistir en disco la cola de mensajes del reloj.
- Pasar la consulta del clima a WorkManager (hoy corre mientras la app está abierta).
- HTTPS y autenticación antes de publicar (hoy `cleartextTrafficPermitted` y sin login).
- Animar el hub (el fondo actual ya incluye al astronauta sentado).
