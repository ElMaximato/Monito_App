import * as sampleRepo from '../repositories/sample.repository.js';
import { requirePet } from './pet.service.js';

export async function addSample(deviceId, sample) {
  const pet = await requirePet(deviceId);
  await sampleRepo.insert(pet.id, { ...sample, recordedAt: new Date(sample.recordedAt) });
}
