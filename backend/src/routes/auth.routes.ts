// src/routes/auth.routes.ts
import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authLimiter } from '../middleware/rateLimiter';

const router = Router();
const authController = new AuthController();

router.post('/register', authLimiter, (req, res, next) => 
  authController.register(req, res, next)
);

router.post('/login', authLimiter, (req, res, next) => 
  authController.login(req, res, next)
);

export default router;
