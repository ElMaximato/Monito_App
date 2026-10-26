-- Datos iniciales: catálogo de estados. Ejecutar después de schema.sql.
USE monito;

INSERT INTO pet_state (code, display_name, priority, sprite) VALUES
  ('SCARED',  'Susto',               1, 'astro_scared'),
  ('PET',     'Caricia',             2, 'astro_excited'),
  ('WALK',    'Caminando',           3, 'astro_idle'),
  ('RUN',     'Corriendo',           3, 'astro_idle'),
  ('CLIMB',   'Trepando',            3, NULL),
  ('EXCITED', 'Emocionado',          4, 'astro_excited'),
  ('CALM',    'Tranquilo',           4, 'astro_sleep'),
  ('RAIN',    'Lluvia',              5, 'astro_rain'),
  ('HOT',     'Calor',               5, 'astro_hot'),
  ('COLD',    'Frío',                5, 'astro_cold'),
  ('NIGHT',   'Noche',               6, 'astro_sleep'),
  ('BORED',   'Aburrido',            7, 'astro_sit'),
  ('IDLE',    'Normal',              8, 'astro_idle')
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  priority     = VALUES(priority),
  sprite       = VALUES(sprite);
