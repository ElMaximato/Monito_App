# Backend (Node.js + Express + MySQL)

Capas (cada una en su carpeta, una responsabilidad por archivo):

| Carpeta         | Qué hace                                                      |
|-----------------|---------------------------------------------------------------|
| `routes/`       | Define URLs y conecta validadores con controladores           |
| `validators/`   | Esquemas zod de lo que entra                                  |
| `controllers/`  | Traduce HTTP ↔ servicios (sin lógica de negocio)              |
| `services/`     | Reglas de negocio (XP, niveles, transacciones)                |
| `repositories/` | Único lugar con SQL                                           |
| `middlewares/`  | Validación, 404 y manejo de errores                           |
| `config/`       | Variables de entorno y pool de MySQL                          |
| `utils/`        | XP/nivel, errores HTTP                                        |

```bash
npm install
copy .env.example .env      # y pon tu contraseña de MySQL
npm run dev                 # http://localhost:3000/api/health
npm test
```
Endpoints en `../docs/API.md`.
