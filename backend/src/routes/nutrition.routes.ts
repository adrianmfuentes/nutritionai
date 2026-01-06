// src/routes/nutrition.routes.ts
import { Router } from 'express';
import { NutritionController } from '../controllers/nutrition.controller';
import { authenticate } from '../middleware/auth';

const router: Router = Router();
const nutritionController = new NutritionController();

// Todas las rutas requieren autenticación
router.use(authenticate);

router.get('/daily', (req, res, next) =>
  nutritionController.getDailySummary(req, res, next)
);

router.get('/weekly', (req, res, next) =>
  nutritionController.getWeeklySummary(req, res, next)
);

router.put('/goals', (req, res, next) =>
  nutritionController.updateGoals(req, res, next)
);

export default router;
