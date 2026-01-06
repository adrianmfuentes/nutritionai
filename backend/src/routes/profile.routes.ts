// src/routes/profile.routes.ts
import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authenticate } from '../middleware/auth';

const router: Router = Router();
const authController = new AuthController();

// Todas las rutas requieren autenticación
router.use(authenticate);

router.get('/', (req, res, next) =>
  authController.getProfile(req, res, next)
);

export default router;
