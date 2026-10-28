/**
 * Resumen de un tramo de tiempo (normalmente un día) a partir del historial de estados
 * y las muestras de sensores. Son funciones puras: se prueban sin base de datos.
 */

/** Estados en los que el reloj detectó movimiento (correr y escalar se ven como caminar). */
export const MOVING_STATES = ['WALK', 'RUN', 'CLIMB'];
/** Estado de "dormir" (modo que activa el usuario). */
export const SLEEP_STATES = ['NIGHT'];

const MS_PER_MINUTE = 60_000;
const round1 = (n) => Math.round(n * 10) / 10;
const toMs = (d) => (d instanceof Date ? d.getTime() : Number(d));

/**
 * logs: [{ state, startedAt, endedAt|null }]. Los tramos se recortan a [from, to];
 * un tramo abierto cuenta hasta min(now, to).
 */
export function summarizeStates(logs, from, to, now) {
  const cap = Math.min(now, to);
  const byState = new Map();

  for (const log of logs) {
    const start = Math.max(toMs(log.startedAt), from);
    const end = Math.min(log.endedAt == null ? cap : toMs(log.endedAt), cap);
    if (end <= start) continue;
    byState.set(log.state, (byState.get(log.state) ?? 0) + (end - start) / MS_PER_MINUTE);
  }

  let moving = 0;
  let sleeping = 0;
  let total = 0;
  for (const [state, minutes] of byState) {
    total += minutes;
    if (MOVING_STATES.includes(state)) moving += minutes;
    else if (SLEEP_STATES.includes(state)) sleeping += minutes;
  }

  return {
    trackedMinutes: round1(total),
    movingMinutes: round1(moving),
    restingMinutes: round1(total - moving - sleeping),
    sleepMinutes: round1(sleeping),
    states: [...byState]
      .map(([state, minutes]) => ({ state, minutes: round1(minutes) }))
      .sort((a, b) => b.minutes - a.minutes),
  };
}

/**
 * El reloj manda el contador de pasos acumulado (desde que se encendió), no los pasos del día.
 * Se suman las diferencias entre muestras; si el contador baja, el reloj se reinició y se
 * cuenta desde cero. baselineSteps = última lectura anterior al tramo (o null si no hay).
 */
export function stepsInRange(samples, baselineSteps = null) {
  let previous = baselineSteps;
  let total = 0;
  for (const sample of samples) {
    if (sample.steps == null) continue;
    if (previous != null) total += sample.steps >= previous ? sample.steps - previous : sample.steps;
    previous = sample.steps;
  }
  return total;
}

/** Promedio, mínimo y máximo del pulso; null si no hay lecturas. */
export function heartRateStats(samples) {
  const values = samples.map((s) => s.heartRate).filter((v) => v != null);
  if (values.length === 0) return null;
  const sum = values.reduce((acc, v) => acc + v, 0);
  return {
    avg: Math.round(sum / values.length),
    min: Math.min(...values),
    max: Math.max(...values),
    samples: values.length,
  };
}

export function buildDailySummary({ logs, samples, baselineSteps, from, to, now = Date.now() }) {
  return {
    from,
    to,
    ...summarizeStates(logs, from, to, now),
    steps: stepsInRange(samples, baselineSteps),
    heartRate: heartRateStats(samples),
  };
}
