// src/routes/profile.routes.ts
import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authenticate } from '../middleware/auth';
import { upload } from '../middleware/upload';

const router: Router = Router();
const authController = new AuthController();

// Todas las rutas requieren autenticación
router.use(authenticate);

router.get('/', (req, res, next) =>
  authController.getProfile(req, res, next)
);

router.put('/', (req, res, next) =>
  authController.updateProfile(req, res, next)
);

router.put('/photo', upload.single('photo'), (req, res, next) =>
  authController.updateProfilePhoto(req, res, next)
);

router.delete('/photo', (req, res, next) =>
  authController.deleteProfilePhoto(req, res, next)
);

export default router;
