// src/routes/meals.routes.ts
import { Router } from 'express';
import { MealsController } from '../controllers/meals.controller';
import { authenticate } from '../middleware/auth';
import { upload } from '../middleware/upload';
import { analysisLimiter } from '../middleware/rateLimiter';

const router: Router = Router();
const mealsController = new MealsController();

// Todas las rutas requieren autenticación
router.use(authenticate);

router.post('/analyze', analysisLimiter, upload.single('image'), (req, res, next) =>
  mealsController.analyzeMeal(req, res, next)
);

router.post('/analyze-text', analysisLimiter, (req, res, next) =>
  mealsController.analyzeTextDescription(req, res, next)
);

router.get('/', (req, res, next) =>
  mealsController.getMeals(req, res, next)
);

router.get('/:mealId', (req, res, next) =>
  mealsController.getMealById(req, res, next)
);

router.patch('/:mealId', (req, res, next) =>
  mealsController.updateMeal(req, res, next)
);

router.delete('/:mealId', (req, res, next) =>
  mealsController.deleteMeal(req, res, next)
);

export default router;
