// src/routes/notifications.routes.ts
import { Router } from 'express';
import { NotificationsController } from '../controllers/notifications.controller';
import { authenticate } from '../middleware/auth';

const router: Router = Router();
const notificationsController = new NotificationsController();

router.use(authenticate);

router.post('/register-device', (req, res, next) =>
  notificationsController.registerDevice(req, res, next)
);

router.delete('/device', (req, res, next) =>
  notificationsController.unregisterDevice(req, res, next)
);

router.patch('/preferences', (req, res, next) =>
  notificationsController.updatePreferences(req, res, next)
);

export default router;
