import test from 'node:test';
import assert from 'node:assert/strict';
import { registerPetBody, eventBody, sampleBody } from '../src/validators/pet.schemas.js';

test('registerPetBody aplica valores por defecto', () => {
  const r = registerPetBody.parse({ deviceId: 'abcdef123456' });
  assert.equal(r.name, 'Monito');
  assert.equal(r.personality, 'curioso');
});

test('eventBody rechaza tipos desconocidos', () => {
  assert.equal(eventBody.safeParse({ type: 'eat', at: 1 }).success, false);
  assert.equal(eventBody.safeParse({ type: 'pet', at: 1700000000000 }).success, true);
});

test('sampleBody acepta campos nulos y rechaza fuera de rango', () => {
  assert.equal(sampleBody.safeParse({ recordedAt: 1700000000000, heartRate: null }).success, true);
  assert.equal(sampleBody.safeParse({ recordedAt: 1700000000000, heartRate: 999 }).success, false);
});
