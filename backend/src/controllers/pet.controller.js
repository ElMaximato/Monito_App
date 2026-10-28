import * as petService from '../services/pet.service.js';
import * as eventService from '../services/event.service.js';
import * as stateService from '../services/state.service.js';
import * as sampleService from '../services/sample.service.js';
import * as summaryService from '../services/summary.service.js';

export async function register(req, res) {
  const { pet, created } = await petService.registerPet(req.body);
  res.status(created ? 201 : 200).json(pet);
}

export async function get(req, res) {
  res.json(await petService.getPet(req.params.deviceId));
}

export async function update(req, res) {
  res.json(await petService.updatePet(req.params.deviceId, req.body));
}

export async function addEvent(req, res) {
  res.status(201).json(await eventService.registerEvent(req.params.deviceId, req.body));
}

export async function addState(req, res) {
  await stateService.logState(req.params.deviceId, req.body);
  res.status(201).json({ ok: true });
}

export async function listStates(req, res) {
  const limit = Math.min(Number(req.query.limit ?? 50) || 50, 200);
  res.json(await stateService.recentStates(req.params.deviceId, limit));
}

export async function getStateMinutes(req, res) {
  res.json(await stateService.stateMinutes(req.params.deviceId));
}

export async function addSample(req, res) {
  await sampleService.addSample(req.params.deviceId, req.body);
  res.status(201).json({ ok: true });
}

export async function getSummary(req, res) {
  res.json(await summaryService.summary(req.params.deviceId, req.query));
}
