import { Router } from 'express';
import { asyncHandler as h } from '../utils/asyncHandler.js';
import { validate } from '../middlewares/validate.js';
import * as c from '../controllers/pet.controller.js';
import {
  deviceParams, registerPetBody, updatePetBody, eventBody, stateLogBody, sampleBody, summaryQuery,
} from '../validators/pet.schemas.js';

export const petRouter = Router();

petRouter.post('/', validate({ body: registerPetBody }), h(c.register));
petRouter.get('/:deviceId', validate({ params: deviceParams }), h(c.get));
petRouter.patch('/:deviceId', validate({ params: deviceParams, body: updatePetBody }), h(c.update));

petRouter.post('/:deviceId/events', validate({ params: deviceParams, body: eventBody }), h(c.addEvent));
petRouter.post('/:deviceId/states', validate({ params: deviceParams, body: stateLogBody }), h(c.addState));
petRouter.get('/:deviceId/states', validate({ params: deviceParams }), h(c.listStates));
petRouter.get('/:deviceId/state-minutes', validate({ params: deviceParams }), h(c.getStateMinutes));
petRouter.post('/:deviceId/samples', validate({ params: deviceParams, body: sampleBody }), h(c.addSample));
petRouter.get('/:deviceId/summary', validate({ params: deviceParams, query: summaryQuery }), h(c.getSummary));
