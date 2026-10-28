import { pool } from '../config/database.js';

export async function insert(petId, s) {
  await pool.execute(
    `INSERT INTO sensor_sample (pet_id, recorded_at, temperature_c, heart_rate, steps, pressure_hpa)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [petId, s.recordedAt, s.temperatureC ?? null, s.heartRate ?? null, s.steps ?? null, s.pressureHpa ?? null],
  );
}

/** Muestras con hora en [from, to), de la más vieja a la más nueva. */
export async function inRange(petId, from, to) {
  const [rows] = await pool.execute(
    `SELECT recorded_at AS recordedAt, heart_rate AS heartRate, steps
       FROM sensor_sample
      WHERE pet_id = ? AND recorded_at >= ? AND recorded_at < ?
      ORDER BY recorded_at`,
    [petId, from, to],
  );
  return rows;
}

/** Última lectura del contador de pasos antes de `before` (null si no hay). */
export async function lastStepsBefore(petId, before) {
  const [rows] = await pool.execute(
    `SELECT steps FROM sensor_sample
      WHERE pet_id = ? AND recorded_at < ? AND steps IS NOT NULL
      ORDER BY recorded_at DESC LIMIT 1`,
    [petId, before],
  );
  return rows[0]?.steps ?? null;
}
