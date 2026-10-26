# Base de datos (MySQL + Workbench)

1. Abre MySQL Workbench y conéctate a tu servidor local (usuario `root` o el que uses).
2. `File > Open SQL Script…` → `schema.sql` → botón del rayo (Execute).
3. Repite con `seed.sql`.
4. Para ver el diagrama: `Database > Reverse Engineer…`, elige el esquema `monito` y genera el EER.

`migrations/` queda para cambios futuros: un archivo por cambio, numerado (`001_xxx.sql`, `002_xxx.sql`),
y nunca se edita el schema original una vez entregado.

Tablas: `pet_state`, `pet_profile`, `pet_stats`, `state_log`, `sensor_sample` y la vista `v_pet_state_minutes`.
