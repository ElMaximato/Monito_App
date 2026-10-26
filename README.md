# Monito

Mascota virtual contextual: vive en un reloj Wear OS, reacciona a la temperatura, la lluvia, la hora,
el movimiento y el pulso, y su hogar (perfil + cuarto) está en la app del celular.

```
monito/
├── android/            Proyecto Android Studio (Gradle multi-módulo)
│   ├── shared/         Código común: estados (PetState) y mensajes reloj ↔ móvil
│   ├── mobile/         App del celular: hub, perfil, cliente del backend y del clima
│   └── wear/           App del reloj: sensores, máquina de estados, pantalla del monito
├── backend/            API REST (Node.js + Express) por capas
├── database/           Scripts MySQL para Workbench (schema, seed, migrations)
├── assets/             Sprites del astronauta y fondo del hub (originales)
└── docs/               Arquitectura, API y plan de pasos
```

## Requisitos (Windows)

- Android Studio reciente (Ladybug 2024.2.1 o superior) con el SDK 35 y una imagen de emulador **Wear OS**
- Node.js 20 o superior
- MySQL 8 + MySQL Workbench

## Puesta en marcha

1. **Base de datos.** En Workbench ejecuta `database/schema.sql` y luego `database/seed.sql`
   (detalle en `database/README.md`).
2. **Backend.**
   ```bat
   cd backend
   npm install
   copy .env.example .env
   rem edita .env con tu contraseña de MySQL
   npm run dev
   ```
   Comprueba `http://localhost:3000/api/health` → `{"status":"ok","db":true}`.
3. **Android.** Abre la carpeta `android/` en Android Studio (File > Open) y espera el *Gradle Sync*.
   No incluye `gradlew`: Android Studio descarga Gradle 8.9 solo, a partir de
   `gradle/wrapper/gradle-wrapper.properties`.
4. **Configura `android/gradle.properties`:**
   - `monito.backendUrl`: con emulador de teléfono deja `http://10.0.2.2:3000/`;
     con el S23 físico pon la IP de tu PC en el Wi-Fi (`http://192.168.x.x:3000/`) y permite el puerto 3000
     en el Firewall de Windows.
   - `monito.weather.latitude` / `longitude`: **cámbialas por tu ciudad** (están en 0.0 como marcador).
5. **Ejecuta** el módulo `mobile` en el S23 y el módulo `wear` en el emulador de Wear OS.
   Para que el emulador del reloj se comunique con el S23 hay que emparejarlos
   (busca la guía oficial "Pair a physical phone with a Wear OS emulator" en developer.android.com).
   Ambas apps comparten `applicationId = com.monito.app`; eso es necesario, no lo cambies.

## Demo sin reloj físico

En el reloj, **pulsación larga** abre el panel de simulación: fuerza cualquier estado
(calor, frío, lluvia, noche, correr, escaleras, pulso…). Un toque = caricia.
Sensores reales del emulador: *Extended controls > Virtual sensors*.

## Estado del proyecto

Esta estructura se generó y revisó **sin compilar Android** (no hay SDK en el entorno de generación).
Sí se ejecutó y probó el backend (arranque, validación, rutas y 7 pruebas). Al primer *Sync* pueden salir
ajustes menores de versiones; la lista de pasos siguientes está en `docs/PASOS.md`.
