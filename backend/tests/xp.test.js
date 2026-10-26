import test from 'node:test';
import assert from 'node:assert/strict';
import { levelForXp, xpForLevel, levelInfo, xpForMinutes, XP_PER_EVENT } from '../src/utils/xp.js';

test('nivel 1 con 0 XP y sube en los umbrales', () => {
  assert.equal(levelForXp(0), 1);
  assert.equal(levelForXp(19), 1);
  assert.equal(levelForXp(20), 2);
  assert.equal(levelForXp(79), 2);
  assert.equal(levelForXp(80), 3);
});

test('xpForLevel es consistente con levelForXp', () => {
  for (let level = 1; level <= 30; level++) {
    assert.equal(levelForXp(xpForLevel(level)), level);
  }
});

test('levelInfo entrega el rango del nivel actual', () => {
  assert.deepEqual(levelInfo(25), { level: 2, currentLevelXp: 20, nextLevelXp: 80 });
});

test('XP por minutos y por evento', () => {
  assert.equal(xpForMinutes(9.9), 0);
  assert.equal(xpForMinutes(10), 1);
  assert.equal(xpForMinutes(35), 3);
  assert.equal(XP_PER_EVENT.pet, 2);
  assert.equal(XP_PER_EVENT.shake, 1);
});
