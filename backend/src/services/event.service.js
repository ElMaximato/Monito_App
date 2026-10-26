import { withTransaction } from '../config/database.js';
import * as petRepo from '../repositories/pet.repository.js';
import * as statsRepo from '../repositories/stats.repository.js';
import { XP_PER_EVENT, levelForXp } from '../utils/xp.js';
import { buildPetResponse, requirePet } from './pet.service.js';

/** Registra una interacción (caricia o sacudida): suma contador y XP. */
export async function registerEvent(deviceId, { type }) {
  return withTransaction(async (conn) => {
    const pet = await requirePet(deviceId, { conn, forUpdate: true });
    await statsRepo.increment(pet.id, type, conn);

    const xp = pet.xp + XP_PER_EVENT[type];
    await petRepo.updateXp(pet.id, xp, levelForXp(xp), conn);

    const fresh = await petRepo.findByDeviceId(deviceId, { conn });
    return buildPetResponse(fresh, conn);
  });
}
