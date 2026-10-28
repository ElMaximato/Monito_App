import * as stateRepo from '../repositories/state.repository.js';
import * as sampleRepo from '../repositories/sample.repository.js';
import { buildDailySummary } from '../utils/dailySummary.js';
import { requirePet } from './pet.service.js';

/** Resumen del tramo [from, to] (epoch ms). El móvil manda el día local completo. */
export async function summary(deviceId, { from, to }) {
  const pet = await requirePet(deviceId);
  const start = new Date(from);
  const end = new Date(to);

  const [logs, samples, baselineSteps] = await Promise.all([
    stateRepo.logsOverlapping(pet.id, start, end),
    sampleRepo.inRange(pet.id, start, end),
    sampleRepo.lastStepsBefore(pet.id, start),
  ]);

  return buildDailySummary({ logs, samples, baselineSteps, from, to, now: Date.now() });
}
