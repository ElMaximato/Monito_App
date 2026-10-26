import { pool } from '../config/database.js';

export async function listStates() {
  const [rows] = await pool.execute(
    'SELECT code, display_name AS displayName, priority, sprite FROM pet_state ORDER BY priority, code',
  );
  return rows;
}
