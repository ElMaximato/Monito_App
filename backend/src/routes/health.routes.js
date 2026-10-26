import { Router } from 'express';
import { asyncHandler } from '../utils/asyncHandler.js';
import { health } from '../controllers/health.controller.js';

export const healthRouter = Router();
healthRouter.get('/', asyncHandler(health));
