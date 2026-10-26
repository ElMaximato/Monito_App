import { Router } from 'express';
import { asyncHandler } from '../utils/asyncHandler.js';
import { listStates } from '../controllers/catalog.controller.js';

export const catalogRouter = Router();
catalogRouter.get('/states', asyncHandler(listStates));
