import { pool } from '../config/database.js';

const run = (conn) => conn ?? pool;

const PROFILE_COLUMNS = `
  id, device_id AS deviceId, name, personality, level, xp,
  created_at AS createdAt, updated_at AS updatedAt`;

export async function findByDeviceId(deviceId, { conn, forUpdate = false } = {}) {
  const [rows] = await run(conn).execute(
    `SELECT ${PROFILE_COLUMNS} FROM pet_profile WHERE device_id = ? ${forUpdate ? 'FOR UPDATE' : ''}`,
    [deviceId],
  );
  return rows[0] ?? null;
}

export async function insert({ deviceId, name, personality }, conn) {
  const [result] = await run(conn).execute(
    'INSERT INTO pet_profile (device_id, name, personality) VALUES (?, ?, ?)',
    [deviceId, name, personality],
  );
  return result.insertId;
}

export async function updateProfile(id, { name, personality }, conn) {
  await run(conn).execute(
    'UPDATE pet_profile SET name = COALESCE(?, name), personality = COALESCE(?, personality) WHERE id = ?',
    [name ?? null, personality ?? null, id],
  );
}

export async function updateXp(id, xp, level, conn) {
  await run(conn).execute('UPDATE pet_profile SET xp = ?, level = ? WHERE id = ?', [xp, level, id]);
}
