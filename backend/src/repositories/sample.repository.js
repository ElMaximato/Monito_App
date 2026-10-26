import { pool } from '../config/database.js';

export async function insert(petId, s) {
  await pool.execute(
    `INSERT INTO sensor_sample (pet_id, recorded_at, temperature_c, heart_rate, steps, pressure_hpa)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [petId, s.recordedAt, s.temperatureC ?? null, s.heartRate ?? null, s.steps ?? null, s.pressureHpa ?? null],
  );
}
