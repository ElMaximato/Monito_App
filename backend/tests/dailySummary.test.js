import test from 'node:test';
import assert from 'node:assert/strict';
import { summarizeStates, stepsInRange, heartRateStats, buildDailySummary } from '../src/utils/dailySummary.js';

const MIN = 60_000;
const DAY_START = Date.UTC(2026, 9, 27, 7, 0, 0); // 00:00 hora local de Sonora (UTC-7)
const DAY_END = DAY_START + 24 * 60 * MIN;

test('separa movimiento, descanso y sueño', () => {
  const logs = [
    { state: 'WALK', startedAt: DAY_START + 60 * MIN, endedAt: DAY_START + 90 * MIN },
    { state: 'RUN', startedAt: DAY_START + 90 * MIN, endedAt: DAY_START + 100 * MIN },
    { state: 'IDLE', startedAt: DAY_START + 100 * MIN, endedAt: DAY_START + 160 * MIN },
    { state: 'NIGHT', startedAt: DAY_START + 160 * MIN, endedAt: DAY_START + 220 * MIN },
  ];
  const r = summarizeStates(logs, DAY_START, DAY_END, DAY_END);
  assert.equal(r.movingMinutes, 40);
  assert.equal(r.restingMinutes, 60);
  assert.equal(r.sleepMinutes, 60);
  assert.equal(r.trackedMinutes, 160);
  assert.deepEqual(r.states[0], { state: 'IDLE', minutes: 60 });
});

test('recorta los tramos que cruzan la medianoche', () => {
  const logs = [
    { state: 'NIGHT', startedAt: DAY_START - 120 * MIN, endedAt: DAY_START + 30 * MIN },
    { state: 'IDLE', startedAt: DAY_END - 10 * MIN, endedAt: DAY_END + 50 * MIN },
  ];
  const r = summarizeStates(logs, DAY_START, DAY_END, DAY_END + 100 * MIN);
  assert.equal(r.sleepMinutes, 30);
  assert.equal(r.restingMinutes, 10);
});

test('el tramo abierto cuenta hasta ahora, no hasta el fin del día', () => {
  const now = DAY_START + 100 * MIN;
  const logs = [{ state: 'WALK', startedAt: DAY_START + 40 * MIN, endedAt: null }];
  const r = summarizeStates(logs, DAY_START, DAY_END, now);
  assert.equal(r.movingMinutes, 60);
});

test('sin historial todo es cero', () => {
  const r = summarizeStates([], DAY_START, DAY_END, DAY_END);
  assert.deepEqual(r, { trackedMinutes: 0, movingMinutes: 0, restingMinutes: 0, sleepMinutes: 0, states: [] });
});

test('pasos: suma diferencias del contador acumulado', () => {
  const samples = [{ steps: 1050 }, { steps: 1200 }, { steps: 1500 }];
  assert.equal(stepsInRange(samples, 1000), 500);
});

test('pasos: sin lectura previa empieza a contar desde la primera muestra', () => {
  assert.equal(stepsInRange([{ steps: 800 }, { steps: 900 }], null), 100);
});

test('pasos: si el contador baja, el reloj se reinició', () => {
  const samples = [{ steps: 2000 }, { steps: 40 }, { steps: 100 }];
  assert.equal(stepsInRange(samples, 1900), 100 + 40 + 60);
});

test('pasos: ignora muestras sin pasos', () => {
  assert.equal(stepsInRange([{ steps: null }, { steps: 10 }, { steps: null }, { steps: 25 }], 5), 20);
});

test('pulso: promedio, mínimo y máximo; null si no hay', () => {
  assert.deepEqual(heartRateStats([{ heartRate: 60 }, { heartRate: 100 }, { heartRate: null }]), {
    avg: 80, min: 60, max: 100, samples: 2,
  });
  assert.equal(heartRateStats([{ heartRate: null }]), null);
  assert.equal(heartRateStats([]), null);
});

test('buildDailySummary junta todo', () => {
  const r = buildDailySummary({
    logs: [{ state: 'WALK', startedAt: DAY_START, endedAt: DAY_START + 10 * MIN }],
    samples: [{ steps: 10, heartRate: 90 }, { steps: 40, heartRate: 110 }],
    baselineSteps: 0,
    from: DAY_START,
    to: DAY_END,
    now: DAY_END,
  });
  assert.equal(r.movingMinutes, 10);
  assert.equal(r.steps, 40);
  assert.equal(r.heartRate.avg, 100);
});
