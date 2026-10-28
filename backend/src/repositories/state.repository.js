import { pool } from '../config/database.js';

const run = (conn) => conn ?? pool;

export async function stateExists(code, conn) {
  const [rows] = await run(conn).execute('SELECT 1 FROM pet_state WHERE code = ?', [code]);
  return rows.length > 0;
}

/** Cierra el tramo abierto (si hay) y devuelve cuántos minutos duró. */
export async function closeOpenLog(petId, endedAt, conn) {
  const [rows] = await run(conn).execute(
    'SELECT id, started_at AS startedAt FROM state_log WHERE pet_id = ? AND ended_at IS NULL ORDER BY started_at DESC LIMIT 1 FOR UPDATE',
    [petId],
  );
  if (rows.length === 0) return 0;
  await run(conn).execute('UPDATE state_log SET ended_at = ? WHERE id = ?', [endedAt, rows[0].id]);
  const minutes = (endedAt.getTime() - new Date(rows[0].startedAt).getTime()) / 60000;
  return Math.max(0, minutes);
}

export async function openLog(petId, stateCode, startedAt, conn) {
  await run(conn).execute(
    'INSERT INTO state_log (pet_id, state_code, started_at) VALUES (?, ?, ?)',
    [petId, stateCode, startedAt],
  );
}

export async function recentLogs(petId, limit) {
  const [rows] = await pool.execute(
    `SELECT id, state_code AS state, started_at AS startedAt, ended_at AS endedAt
       FROM state_log WHERE pet_id = ? ORDER BY started_at DESC LIMIT ${Number(limit)}`,
    [petId],
  );
  return rows;
}

export async function minutesByState(petId) {
  const [rows] = await pool.execute(
    'SELECT state_code AS state, minutes FROM v_pet_state_minutes WHERE pet_id = ? ORDER BY minutes DESC',
    [petId],
  );
  return rows.map((r) => ({ state: r.state, minutes: Number(r.minutes) }));
}

/** Tramos que tocan [from, to] (los abiertos incluidos). El recorte lo hace utils/dailySummary. */
export async function logsOverlapping(petId, from, to) {
  const [rows] = await pool.execute(
    `SELECT state_code AS state, started_at AS startedAt, ended_at AS endedAt
       FROM state_log
      WHERE pet_id = ? AND started_at < ? AND (ended_at IS NULL OR ended_at > ?)
      ORDER BY started_at`,
    [petId, to, from],
  );
  return rows;
}
