import { badRequest } from '../utils/httpError.js';

/** validate({ body: zodSchema, params: zodSchema, query: zodSchema }) */
export const validate = (schemas) => (req, _res, next) => {
  for (const part of ['params', 'query', 'body']) {
    if (!schemas[part]) continue;
    const parsed = schemas[part].safeParse(req[part]);
    if (!parsed.success) {
      return next(badRequest('Datos inválidos', parsed.error.flatten().fieldErrors));
    }
    // En Express 4 se puede reemplazar sin problema
    req[part] = parsed.data;
  }
  next();
};
