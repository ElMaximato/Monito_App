import { withTransaction } from '../config/database.js';
import * as petRepo from '../repositories/pet.repository.js';
import * as stateRepo from '../repositories/state.repository.js';
import { badRequest } from '../utils/httpError.js';
import { levelForXp, xpForMinutes } from '../utils/xp.js';
import { requirePet } from './pet.service.js';

/**
 * El reloj avisa cada vez que cambia de estado. Se cierra el tramo anterior,
 * se suma XP por el tiempo que duró y se abre el nuevo.
 */
export async function logState(deviceId, { state, startedAt }) {
  if (!(await stateRepo.stateExists(state))) {
    throw badRequest(`Estado desconocido: ${state}`);
  }
  const start = new Date(startedAt);

  await withTransaction(async (conn) => {
    const pet = await requirePet(deviceId, { conn, forUpdate: true });
    const minutes = await stateRepo.closeOpenLog(pet.id, start, conn);

    const gained = xpForMinutes(minutes);
    if (gained > 0) {
      const xp = pet.xp + gained;
      await petRepo.updateXp(pet.id, xp, levelForXp(xp), conn);
    }
    await stateRepo.openLog(pet.id, state, start, conn);
  });
}

export async function recentStates(deviceId, limit) {
  const pet = await requirePet(deviceId);
  return stateRepo.recentLogs(pet.id, limit);
}

export async function stateMinutes(deviceId) {
  const pet = await requirePet(deviceId);
  return stateRepo.minutesByState(pet.id);
}
