import { notFound } from '../utils/httpError.js';

export const routeNotFound = (req, _res, next) => next(notFound(`Ruta no encontrada: ${req.method} ${req.originalUrl}`));
