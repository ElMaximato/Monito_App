-- Datos iniciales: catálogo de estados. Ejecutar después de schema.sql.
USE monito;

INSERT INTO pet_state (code, display_name, priority, sprite) VALUES
  ('PET',     'Caricia',             1, 'astro_excited'),
  ('NIGHT',   'Dormir',              2, 'astro_sleep'),
  ('SCARED',  'Susto',               3, 'astro_scared'),
  ('WALK',    'Caminando',           4, 'astro_idle'),
  ('RUN',     'Corriendo',           4, 'astro_idle'),
  ('CLIMB',   'Trepando',            4, NULL),
  ('EXCITED', 'Emocionado',          5, 'astro_excited'),
  ('CALM',    'Tranquilo',           5, 'astro_sleep'),
  ('RAIN',    'Lluvia',              6, 'astro_rain'),
  ('HOT',     'Calor',               6, 'astro_hot'),
  ('COLD',    'Frío',                6, 'astro_cold'),
  ('BORED',   'Aburrido',            7, 'astro_sit'),
  ('IDLE',    'Normal',              8, 'astro_idle')
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  priority     = VALUES(priority),
  sprite       = VALUES(sprite);
