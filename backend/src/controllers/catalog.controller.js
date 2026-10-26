import * as catalogService from '../services/catalog.service.js';

export async function listStates(_req, res) {
  res.json(await catalogService.listStates());
}
