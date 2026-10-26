import { pingDatabase } from '../config/database.js';

export async function health(_req, res) {
  const db = await pingDatabase();
  res.status(db ? 200 : 503).json({ status: db ? 'ok' : 'degraded', db });
}
