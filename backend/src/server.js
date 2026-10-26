import { createApp } from './app.js';
import { env } from './config/env.js';
import { pingDatabase } from './config/database.js';

const app = createApp();

app.listen(env.port, '0.0.0.0', async () => {
  console.log(`Monito API escuchando en http://0.0.0.0:${env.port}`);
  const ok = await pingDatabase();
  console.log(ok ? 'MySQL: conectado' : 'MySQL: SIN conexión (revisa .env y que el servicio esté activo)');
});
