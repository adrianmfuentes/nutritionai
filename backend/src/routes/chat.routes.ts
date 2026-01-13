// src/routes/chat.routes.ts
import { Router } from 'express';
import { ChatController } from '../controllers/chat.controller';
import { authenticate } from '../middleware/auth';

const router: Router = Router();
const chatController = new ChatController();

// Todas las rutas requieren autenticación
router.use(authenticate);

router.post('/', (req, res, next) =>
  chatController.chat(req, res, next)
);

export default router;
