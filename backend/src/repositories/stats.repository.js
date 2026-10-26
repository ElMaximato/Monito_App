import { pool } from '../config/database.js';

const run = (conn) => conn ?? pool;

// Lista blanca: nunca se interpola input del usuario en el SQL.
const COLUMN_BY_EVENT = Object.freeze({ pet: 'pet_count', shake: 'shake_count' });

export async function createFor(petId, conn) {
  await run(conn).execute('INSERT INTO pet_stats (pet_id) VALUES (?)', [petId]);
}

export async function findByPetId(petId, conn) {
  const [rows] = await run(conn).execute(
    'SELECT pet_count AS petCount, shake_count AS shakeCount FROM pet_stats WHERE pet_id = ?',
    [petId],
  );
  return rows[0] ?? { petCount: 0, shakeCount: 0 };
}

export async function increment(petId, eventType, conn) {
  const column = COLUMN_BY_EVENT[eventType];
  if (!column) throw new Error(`Tipo de evento desconocido: ${eventType}`);
  await run(conn).execute(`UPDATE pet_stats SET ${column} = ${column} + 1 WHERE pet_id = ?`, [petId]);
}
