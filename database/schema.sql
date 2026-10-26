-- =====================================================================
-- Monito · esquema de base de datos (MySQL 8)
-- Ejecutar completo en MySQL Workbench: File > Open SQL Script > Rayo.
-- Es re-ejecutable: usa IF NOT EXISTS y no borra datos.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS monito
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE monito;

-- Catálogo de estados del personaje (espejo de PetState en el módulo shared).
CREATE TABLE IF NOT EXISTS pet_state (
  code          VARCHAR(16)  NOT NULL,
  display_name  VARCHAR(48)  NOT NULL,
  priority      TINYINT UNSIGNED NOT NULL COMMENT '1 = gana sobre todos; mayor número = menor prioridad',
  sprite        VARCHAR(32)  NULL COMMENT 'NULL = sprite pendiente',
  PRIMARY KEY (code)
) ENGINE=InnoDB;

-- Perfil del monito. Un monito por teléfono (device_id), sin login en el MVP.
CREATE TABLE IF NOT EXISTS pet_profile (
  id           INT UNSIGNED NOT NULL AUTO_INCREMENT,
  device_id    VARCHAR(64)  NOT NULL,
  name         VARCHAR(40)  NOT NULL,
  personality  VARCHAR(24)  NOT NULL DEFAULT 'curioso',
  level        INT UNSIGNED NOT NULL DEFAULT 1,
  xp           INT UNSIGNED NOT NULL DEFAULT 0,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pet_profile_device (device_id)
) ENGINE=InnoDB;

-- Contadores de interacciones (las estadísticas por estado salen de state_log).
CREATE TABLE IF NOT EXISTS pet_stats (
  pet_id       INT UNSIGNED NOT NULL,
  pet_count    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'caricias',
  shake_count  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'sacudidas',
  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (pet_id),
  CONSTRAINT fk_pet_stats_pet FOREIGN KEY (pet_id) REFERENCES pet_profile (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Historial de estados: una fila por tramo (ended_at NULL = estado actual).
CREATE TABLE IF NOT EXISTS state_log (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  pet_id      INT UNSIGNED NOT NULL,
  state_code  VARCHAR(16)  NOT NULL,
  started_at  DATETIME(3)  NOT NULL,
  ended_at    DATETIME(3)  NULL,
  PRIMARY KEY (id),
  KEY idx_state_log_pet_started (pet_id, started_at),
  CONSTRAINT fk_state_log_pet   FOREIGN KEY (pet_id)     REFERENCES pet_profile (id) ON DELETE CASCADE,
  CONSTRAINT fk_state_log_state FOREIGN KEY (state_code) REFERENCES pet_state (code)
) ENGINE=InnoDB;

-- Muestras de sensores enviadas por el reloj (cada ~1 min).
CREATE TABLE IF NOT EXISTS sensor_sample (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  pet_id         INT UNSIGNED NOT NULL,
  recorded_at    DATETIME(3)  NOT NULL,
  temperature_c  DECIMAL(4,1) NULL,
  heart_rate     SMALLINT UNSIGNED NULL,
  steps          INT UNSIGNED NULL,
  pressure_hpa   DECIMAL(6,1) NULL,
  PRIMARY KEY (id),
  KEY idx_sensor_sample_pet_time (pet_id, recorded_at),
  CONSTRAINT fk_sensor_sample_pet FOREIGN KEY (pet_id) REFERENCES pet_profile (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Minutos acumulados por estado (para el perfil del móvil).
CREATE OR REPLACE VIEW v_pet_state_minutes AS
SELECT
  pet_id,
  state_code,
  ROUND(SUM(TIMESTAMPDIFF(SECOND, started_at, COALESCE(ended_at, NOW(3)))) / 60, 1) AS minutes
FROM state_log
GROUP BY pet_id, state_code;
