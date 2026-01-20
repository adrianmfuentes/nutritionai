// src/routes/auth.routes.ts
import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authLimiter } from '../middleware/rateLimiter';
import { authenticate } from '../middleware/auth';

const router: Router = Router();
const authController = new AuthController();

router.post('/register', authLimiter, (req, res, next) => 
  authController.register(req, res, next)
);

router.post('/login', authLimiter, (req, res, next) => 
  authController.login(req, res, next)
);

router.post('/send-verification', (req, res, next) =>
  authController.sendVerificationEmail(req, res, next)
);

router.post('/verify-email', (req, res, next) =>
  authController.verifyEmail(req, res, next)
);

router.post('/change-password', authenticate, (req, res, next) =>
  authController.changePassword(req, res, next)
);

router.delete('/delete-account', authenticate, (req, res, next) =>
  authController.deleteAccount(req, res, next)
);

export default router;
