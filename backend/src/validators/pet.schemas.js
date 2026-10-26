import { z } from 'zod';

const deviceId = z.string().trim().min(8).max(64);
const epochMillis = z.number().int().positive();

export const deviceParams = z.object({ deviceId });

export const registerPetBody = z.object({
  deviceId,
  name: z.string().trim().min(1).max(40).default('Monito'),
  personality: z.string().trim().min(1).max(24).default('curioso'),
});

export const updatePetBody = z
  .object({
    name: z.string().trim().min(1).max(40).optional(),
    personality: z.string().trim().min(1).max(24).optional(),
  })
  .refine((v) => v.name !== undefined || v.personality !== undefined, { message: 'Nada que actualizar' });

export const eventBody = z.object({
  type: z.enum(['pet', 'shake']),
  at: epochMillis,
});

export const stateLogBody = z.object({
  state: z.string().trim().min(1).max(16),
  startedAt: epochMillis,
});

export const sampleBody = z.object({
  recordedAt: epochMillis,
  temperatureC: z.number().min(-60).max(80).nullish(),
  heartRate: z.number().int().min(20).max(250).nullish(),
  steps: z.number().int().min(0).nullish(),
  pressureHpa: z.number().min(300).max(1100).nullish(),
});
