import { Router } from 'express';
import { healthRouter } from './health.routes.js';
import { catalogRouter } from './catalog.routes.js';
import { petRouter } from './pet.routes.js';

export const apiRouter = Router();
apiRouter.use('/health', healthRouter);
apiRouter.use('/', catalogRouter);
apiRouter.use('/pets', petRouter);
