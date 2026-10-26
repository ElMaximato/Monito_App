import { withTransaction } from '../config/database.js';
import * as petRepo from '../repositories/pet.repository.js';
import * as statsRepo from '../repositories/stats.repository.js';
import { levelInfo } from '../utils/xp.js';
import { notFound } from '../utils/httpError.js';

/** Perfil + progreso de nivel + contadores, en el formato que consume el móvil. */
export async function buildPetResponse(pet, conn) {
  const stats = await statsRepo.findByPetId(pet.id, conn);
  const { level, currentLevelXp, nextLevelXp } = levelInfo(pet.xp);
  return {
    id: pet.id,
    deviceId: pet.deviceId,
    name: pet.name,
    personality: pet.personality,
    level,
    xp: pet.xp,
    currentLevelXp,
    nextLevelXp,
    stats,
  };
}

export async function requirePet(deviceId, options) {
  const pet = await petRepo.findByDeviceId(deviceId, options);
  if (!pet) throw notFound('Ese dispositivo todavía no tiene monito');
  return pet;
}

/** Idempotente: si el dispositivo ya tiene monito, lo devuelve. */
export async function registerPet({ deviceId, name, personality }) {
  const existing = await petRepo.findByDeviceId(deviceId);
  if (existing) return { pet: await buildPetResponse(existing), created: false };

  try {
    const pet = await withTransaction(async (conn) => {
      const id = await petRepo.insert({ deviceId, name, personality }, conn);
      await statsRepo.createFor(id, conn);
      return petRepo.findByDeviceId(deviceId, { conn });
    });
    return { pet: await buildPetResponse(pet), created: true };
  } catch (err) {
    // Dos peticiones simultáneas: la segunda choca con el UNIQUE y devuelve la existente.
    if (err?.code === 'ER_DUP_ENTRY') {
      return { pet: await buildPetResponse(await requirePet(deviceId)), created: false };
    }
    throw err;
  }
}

export async function getPet(deviceId) {
  return buildPetResponse(await requirePet(deviceId));
}

export async function updatePet(deviceId, changes) {
  const pet = await requirePet(deviceId);
  await petRepo.updateProfile(pet.id, changes);
  return buildPetResponse(await requirePet(deviceId));
}
