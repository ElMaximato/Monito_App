// Reglas PROVISIONALES de XP y nivel (se afinan en la fase 3 del MVP).
// Mantener aquí: el backend es la única fuente de verdad; el móvil solo muestra.

export const XP_PER_EVENT = Object.freeze({ pet: 2, shake: 1 });
export const MINUTES_PER_STATE_XP = 10; // 1 XP por cada 10 min en un estado

/** XP total necesaria para ALCANZAR el nivel n (n >= 1). */
export const xpForLevel = (level) => 20 * (level - 1) ** 2;

export const levelForXp = (xp) => Math.floor(Math.sqrt(xp / 20)) + 1;

export const xpForMinutes = (minutes) => Math.floor(minutes / MINUTES_PER_STATE_XP);

export function levelInfo(xp) {
  const level = levelForXp(xp);
  return { level, currentLevelXp: xpForLevel(level), nextLevelXp: xpForLevel(level + 1) };
}
