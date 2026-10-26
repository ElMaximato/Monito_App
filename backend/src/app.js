import express from 'express';
import cors from 'cors';
import morgan from 'morgan';
import { apiRouter } from './routes/index.js';
import { routeNotFound } from './middlewares/notFound.js';
import { errorHandler } from './middlewares/errorHandler.js';

export function createApp() {
  const app = express();
  app.use(cors());
  app.use(express.json({ limit: '100kb' }));
  app.use(morgan('dev'));

  app.use('/api', apiRouter);

  app.use(routeNotFound);
  app.use(errorHandler);
  return app;
}
