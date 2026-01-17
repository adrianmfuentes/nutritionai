// src/controllers/meals.controller.ts
import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/database';
import { VisionService } from '../services/vision.service';
import { StorageService } from '../services/storage.service';
import { logger } from '../utils/logger';
import { z } from 'zod';
import fs from 'fs/promises';
import { HttpError } from '../utils/httpError';
import { isLikelyMealDescription } from '../utils/mealHeuristics';

const AnalyzeMealSchema = z.object({
  mealType: z.enum(['breakfast', 'lunch', 'dinner', 'snack']).optional(),
  timestamp: z.string().optional(),
});

const AnalyzeTextSchema = z.object({
  description: z.string().min(1, 'La descripción es requerida'),
  mealType: z.enum(['breakfast', 'lunch', 'dinner', 'snack']).optional(),
  timestamp: z.string().optional(),
});

const FoodSchema = z.object({
  id: z.string().optional(),
  name: z.string(),
  confidence: z.number().optional(),
  portion: z.object({
    amount: z.number(),
    unit: z.string(),
  }),
  nutrition: z.object({
    calories: z.number(),
    protein: z.number(),
    carbs: z.number(),
    fat: z.number(),
    fiber: z.number().optional(),
  }),
  category: z.string(),
});

const UpdateMealSchema = z.object({
  notes: z.string().nullable().optional(),
  mealType: z.string().nullable().optional(),
  foods: z.array(FoodSchema).optional(),
});

export class MealsController {
  private visionService: VisionService;
  private storageService: StorageService;

  constructor() {
    this.visionService = new VisionService();
    this.storageService = new StorageService();
  }

  private normalizeMealType(type: string | null | undefined): 'breakfast' | 'lunch' | 'dinner' | 'snack' {
    const raw = (type || '').toLowerCase().trim();
    const mapped =
      raw === 'desayuno' ? 'breakfast' :
      (raw === 'almuerzo' || raw === 'comida') ? 'lunch' :
      raw === 'cena' ? 'dinner' :
      (raw === 'merienda' || raw === 'snacks') ? 'snack' :
      raw;

    const valid = ['breakfast', 'lunch', 'dinner', 'snack'] as const;
    return (valid as readonly string[]).includes(mapped) ? (mapped as any) : 'snack';
  }

  private computeHealthScoreFromTotals(totals: { calories?: any; protein?: any; carbs?: any; fat?: any; fiber?: any }): number {
      /**
       * Genera un consejo personalizado según la puntuación de salud y los nutrientes
       */
  
    // Fórmula avanzada basada en criterios científicos
    const calories = Number(totals.calories ?? 0);
    const protein = Number(totals.protein ?? 0);
    const carbs = Number(totals.carbs ?? 0);
    const fat = Number(totals.fat ?? 0);
    const fiber = Number(totals.fiber ?? 0);

    // 1. Penalización por exceso de calorías
    let score = 10;
    if (calories > 800) score -= 2;
    else if (calories > 600) score -= 1;
    if (calories < 200) score -= 1; // muy baja en calorías

    // 2. Proporción proteína/calorías (ideal: >15% de calorías de proteína)
    const proteinKcal = protein * 4;
    const proteinPct = calories > 0 ? (proteinKcal / calories) : 0;
    if (proteinPct >= 0.15) score += 1;
    else if (proteinPct < 0.10) score -= 1;

    // 3. Proporción fibra/calorías (ideal: >8g por 500 kcal)
    const fiberPer500kcal = calories > 0 ? (fiber / calories) * 500 : 0;
    if (fiberPer500kcal >= 8) score += 1;
    else if (fiberPer500kcal < 3) score -= 1;

    // 4. Relación proteína/carbohidrato (ideal: 0.3-1)
    const protCarbRatio = carbs > 0 ? protein / carbs : 0;
    if (protCarbRatio >= 0.3 && protCarbRatio <= 1.2) score += 1;
    else if (protCarbRatio < 0.2) score -= 1;

    // 5. Penalización por exceso de grasa (>30g)
    if (fat > 30) score -= 1;
    // Penalización por grasa/caloría (>35% kcal de grasa)
    const fatKcal = fat * 9;
    const fatPct = calories > 0 ? (fatKcal / calories) : 0;
    if (fatPct > 0.35) score -= 1;

    // 6. Bonus por variedad (si hay proteína, vegetal y carbohidrato)
    // (esto se puede mejorar si se pasa la lista de alimentos, pero aquí solo con totales)
    if (protein > 10 && carbs > 15 && fiber > 3) score += 1;

    // Clamp final
    score = Math.round(score);
    return Math.max(1, Math.min(10, Number.isFinite(score) ? score : 5));
  }
  
  private getHealthAdvice(score: number, totals: { calories?: any; protein?: any; carbs?: any; fat?: any; fiber?: any }): string {
    if (score >= 9) return '¡Excelente comida! Muy equilibrada y saludable.';
    if (score >= 7) return 'Buena elección. Puedes añadir más vegetales o fibra para mejorar aún más.';
    if (score >= 5) return 'Comida aceptable, pero podrías mejorar el balance añadiendo proteína magra, vegetales o reduciendo grasas.';
    if (score >= 3) return 'Intenta reducir grasas y calorías, y aumentar la fibra y proteína para una comida más saludable.';
    return 'Esta comida es poco saludable. Intenta incluir más vegetales, proteína magra y reducir azúcares y grasas.';
  }
  
  private ensureHealthScore(analysis: any): number {
    // SIEMPRE calcular la puntuación en backend, ignorar la del modelo
    return this.computeHealthScoreFromTotals({
      calories: analysis?.totalNutrition?.calories,
      protein: analysis?.totalNutrition?.protein,
      carbs: analysis?.totalNutrition?.carbs,
      fat: analysis?.totalNutrition?.fat,
      fiber: analysis?.totalNutrition?.fiber,
    });
  }

  async analyzeMeal(req: Request, res: Response, next: NextFunction) {
    const client = await pool.connect();
    let transactionStarted = false;
    let imagePath = req.file?.path;
    try {
      const userId = (req as any).user?.id;
      if (!imagePath) {
        return res.status(400).json({ error: 'No se proporcionó imagen' });
      }
      const validatedData = AnalyzeMealSchema.parse(req.body);
      const { mealType, timestamp } = validatedData;
      logger.info(`Analizando comida para usuario: ${userId}`);
      // Analizar imagen con IA
      const analysis = await this.visionService.analyzeMealImage(imagePath);
      if (!analysis.foods || analysis.foods.length === 0) {
        throw new HttpError(
          422,
          'No se detectaron alimentos en la imagen. Intenta con otra foto más clara.',
          'MEAL_NOT_DETECTED',
          { inputType: 'image' }
        );
      }
      // Guardar imagen permanentemente
      const imageUrl = await this.storageService.saveImage(imagePath, userId);
      // Eliminar imagen temporal después de guardar (solo si está bajo uploads)
      const path = require('path');
      const uploadsRoot = path.resolve(process.env.UPLOAD_PATH || './uploads');
      const absoluteImagePath = path.resolve(imagePath);
      if (absoluteImagePath.startsWith(uploadsRoot + path.sep)) {
        await fs.unlink(absoluteImagePath).catch(() => undefined);
      } else {
        logger.warn('Intento de eliminar archivo fuera de uploads bloqueado:', imagePath);
      }
      imagePath = undefined;
      // Iniciar transacción
      await client.query('BEGIN');
      transactionStarted = true;
      let consumedAt = new Date();
      if (timestamp) {
        // Si es solo dígitos, asumimos milisegundos
        if (/^\d+$/.test(timestamp)) {
          consumedAt = new Date(parseInt(timestamp));
        } else {
          consumedAt = new Date(timestamp);
        }
      }
      // Verificamos si la fecha es inválida
      if (isNaN(consumedAt.getTime())) {
        logger.warn(`Timestamp inválido recibido: ${timestamp}, usando fecha actual`);
        consumedAt = new Date();
      }
      const mealDate = consumedAt.toISOString().split('T')[0];
      // Sanitize meal type
      const sanitizeMealType = (type: string) => this.normalizeMealType(type);
      const healthScore = this.ensureHealthScore(analysis);
      if (!analysis.mealContext) {
        analysis.mealContext = { estimatedMealType: 'snack', portionSize: 'medium', healthScore };
      } else {
        analysis.mealContext.healthScore = healthScore;
      }
      // Insertar meal
      const mealResult = await client.query(
        `INSERT INTO meals (
          user_id, meal_type, image_url, total_calories, total_protein, 
          total_carbs, total_fat, total_fiber, health_score, meal_date, consumed_at
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
        RETURNING *`,
        [
          userId,
          sanitizeMealType(mealType || analysis.mealContext.estimatedMealType),
          imageUrl,
          analysis.totalNutrition.calories,
          analysis.totalNutrition.protein,
          analysis.totalNutrition.carbs,
          analysis.totalNutrition.fat,
          analysis.totalNutrition.fiber || null,
          healthScore,
          mealDate,
          consumedAt,
        ]
      );
      const meal = mealResult.rows[0];
      // Sanitize functions
      const validCategories = ['protein', 'carb', 'vegetable', 'fruit', 'dairy', 'fat', 'mixed'];
      const sanitizeCategory = (category: string) => {
        const lowerCat = (category || 'mixed').toLowerCase();
        return validCategories.includes(lowerCat) ? lowerCat : 'mixed';
      };
      const clampConfidence = (conf: number) => Math.max(0, Math.min(1, conf));
      const clampPortion = (amount: number) => Math.max(0, Math.min(9999.99, amount));
      // Insertar detected foods
      const foodInsertPromises = analysis.foods.map((food) =>
        client.query(
          `INSERT INTO detected_foods (
            meal_id, name, confidence, portion_amount, portion_unit,
            calories, protein, carbs, fat, fiber, category
          ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
          RETURNING *`,
          [
            meal.id,
            food.name,
            clampConfidence(food.confidence),
            clampPortion(food.portion.amount),
            food.portion.unit,
            food.nutrition.calories,
            food.nutrition.protein,
            food.nutrition.carbs,
            food.nutrition.fat,
            food.nutrition.fiber || null,
            sanitizeCategory(food.category),
          ]
        )
      );
      const foodResults = await Promise.all(foodInsertPromises);
      const detectedFoods = foodResults.map((r) => r.rows[0]);
      await client.query('COMMIT');
      logger.info(`Comida analizada exitosamente: ${meal.id}`);
      res.status(201).json({
        mealId: meal.id,
        detectedFoods: detectedFoods.map((food) => ({
          name: food.name,
          confidence: parseFloat(food.confidence),
          portion: {
            amount: parseFloat(food.portion_amount),
            unit: food.portion_unit,
          },
          nutrition: {
            calories: food.calories,
            protein: parseFloat(food.protein),
            carbs: parseFloat(food.carbs),
            fat: parseFloat(food.fat),
            fiber: food.fiber ? parseFloat(food.fiber) : undefined,
          },
          category: food.category,
        })),
        totalNutrition: {
          calories: meal.total_calories,
          protein: parseFloat(meal.total_protein),
          carbs: parseFloat(meal.total_carbs),
          fat: parseFloat(meal.total_fat),
          fiber: meal.total_fiber ? parseFloat(meal.total_fiber) : undefined,
        },
        imageUrl: meal.image_url,
        timestamp: meal.consumed_at,
        mealContext: analysis.mealContext,
        notes: analysis.notes,
      });
    } catch (error) {
      if (transactionStarted) {
        await client.query('ROLLBACK').catch(() => undefined);
      }
      // Si la imagen temporal aún existe (fallo antes de saveImage), intentamos limpiarla.
      if (imagePath) {
        // Sanitizar imagePath: solo permitir rutas bajo /uploads o ./uploads
        const path = require('path');
        const uploadsRoot = path.resolve(process.env.UPLOAD_PATH || './uploads');
        const absoluteImagePath = path.resolve(imagePath);
        if (absoluteImagePath.startsWith(uploadsRoot + path.sep)) {
          await fs.unlink(absoluteImagePath).catch(() => undefined);
        } else {
          logger.warn('Intento de eliminar archivo fuera de uploads bloqueado:', imagePath);
        }
      }
      logger.error('Error analizando comida:', error);
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          code: 'VALIDATION_FAILED',
          details: error.issues 
        });
      }
      next(error);
    } finally {
      client.release();
    }
  }

  async getMeals(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { date, limit = 20, offset = 0 } = req.query;

      let query = `
        SELECT m.*, 
               json_agg(
                 json_build_object(
                   'id', df.id,
                   'name', df.name,
                   'confidence', df.confidence,
                   'portion', json_build_object('amount', df.portion_amount, 'unit', df.portion_unit),
                   'nutrition', json_build_object(
                     'calories', df.calories,
                     'protein', df.protein,
                     'carbs', df.carbs,
                     'fat', df.fat,
                     'fiber', df.fiber
                   ),
                   'category', df.category
                 )
               ) as foods
        FROM meals m
        LEFT JOIN detected_foods df ON m.id = df.meal_id
        WHERE m.user_id = $1
      `;

      const params: any[] = [userId];
      let paramIndex = 2;

      if (date) {
        query += ` AND m.meal_date = $${paramIndex}`;
        params.push(date);
        paramIndex++;
      }

      query += `
        GROUP BY m.id
        ORDER BY m.consumed_at DESC
        LIMIT $${paramIndex} OFFSET $${paramIndex + 1}
      `;
      params.push(parseInt(limit as string), parseInt(offset as string));

      const result = await pool.query(query, params);

      // Contar total
      let countQuery = 'SELECT COUNT(*) FROM meals WHERE user_id = $1';
      const countParams: any[] = [userId];
      
      if (date) {
        countQuery += ' AND meal_date = $2';
        countParams.push(date);
      }

      const countResult = await pool.query(countQuery, countParams);
      const total = parseInt(countResult.rows[0].count);

      // Mapear meals al formato esperado por el frontend
      const meals = await Promise.all(result.rows.map(async (meal: any) => {
        const parsedHealth = meal.health_score !== null && meal.health_score !== undefined
          ? parseFloat(meal.health_score)
          : this.computeHealthScoreFromTotals({
              calories: meal.total_calories,
              protein: meal.total_protein,
              carbs: meal.total_carbs,
              fat: meal.total_fat,
              fiber: meal.total_fiber,
            });

        if (meal.health_score === null || meal.health_score === undefined) {
          try {
            await pool.query(
              'UPDATE meals SET health_score = $1 WHERE id = $2 AND user_id = $3',
              [parsedHealth, meal.id, userId]
            );
          } catch {
            // ignore
          }
        }

        return {
          mealId: meal.id,
          mealType: meal.meal_type,
          imageUrl: meal.image_url,
          totalCalories: meal.total_calories,
          totalProtein: meal.total_protein !== null && meal.total_protein !== undefined ? parseFloat(meal.total_protein) : null,
          totalCarbs: meal.total_carbs !== null && meal.total_carbs !== undefined ? parseFloat(meal.total_carbs) : null,
          totalFat: meal.total_fat !== null && meal.total_fat !== undefined ? parseFloat(meal.total_fat) : null,
          totalFiber: meal.total_fiber !== null && meal.total_fiber !== undefined ? parseFloat(meal.total_fiber) : null,
          healthScore: parsedHealth,
          notes: meal.notes,
          timestamp: meal.consumed_at,
        };
      }));

      res.json({
        meals,
        pagination: {
          total,
          page: Math.floor(parseInt(offset as string) / parseInt(limit as string)) + 1,
        },
      });
    } catch (error) {
      next(error);
    }
  }

  async getMealById(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { mealId } = req.params;

      const result = await pool.query(
        `SELECT m.*, 
                json_agg(
                  json_build_object(
                    'id', df.id,
                    'name', df.name,
                    'confidence', df.confidence,
                    'portion', json_build_object('amount', df.portion_amount, 'unit', df.portion_unit),
                    'nutrition', json_build_object(
                      'calories', df.calories,
                      'protein', df.protein,
                      'carbs', df.carbs,
                      'fat', df.fat,
                      'fiber', df.fiber
                    ),
                    'category', df.category
                  )
                ) as foods
         FROM meals m
         LEFT JOIN detected_foods df ON m.id = df.meal_id
         WHERE m.id = $1 AND m.user_id = $2
         GROUP BY m.id`,
        [mealId, userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ error: 'Comida no encontrada' });
      }

      res.json({ meal: result.rows[0] });
    } catch (error) {
      next(error);
    }
  }

  async updateMeal(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { mealId } = req.params;
      const validated = UpdateMealSchema.parse(req.body);
      const notes = validated.notes ?? null;
      const mealType = validated.mealType ?? null;
      const foods = validated.foods ?? [];

      // Si no hay alimentos, eliminar la comida y sus alimentos
      if (foods.length === 0) {
        await pool.query('DELETE FROM detected_foods WHERE meal_id = $1', [mealId]);
        await pool.query('DELETE FROM meals WHERE id = $1 AND user_id = $2', [mealId, userId]);
        return res.json({ success: true, deleted: true });
      }

      // Actualizar comida
      // Recalcular totales
      const total = foods.reduce((acc, f) => ({
        calories: acc.calories + f.nutrition.calories,
        protein: acc.protein + f.nutrition.protein,
        carbs: acc.carbs + f.nutrition.carbs,
        fat: acc.fat + f.nutrition.fat,
        fiber: (acc.fiber ?? 0) + (f.nutrition.fiber ?? 0)
      }), { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 });

      const computedHealth = this.computeHealthScoreFromTotals(total);
      const advice = this.getHealthAdvice(computedHealth, total);

      const result = await pool.query(
        `UPDATE meals 
         SET notes = $1, meal_type = COALESCE($2, meal_type), health_score = $3, total_calories = $4, total_protein = $5, total_carbs = $6, total_fat = $7, total_fiber = $8, updated_at = CURRENT_TIMESTAMP 
         WHERE id = $9 AND user_id = $10 
         RETURNING *`,
        [notes, mealType ? this.normalizeMealType(mealType) : null, computedHealth, total.calories, total.protein, total.carbs, total.fat, total.fiber, mealId, userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ error: 'Comida no encontrada' });
      }

      // Actualizar alimentos: eliminar los existentes y crear los nuevos
      await pool.query('DELETE FROM detected_foods WHERE meal_id = $1', [mealId]);
      for (const food of foods) {
        await pool.query(
          `INSERT INTO detected_foods (meal_id, name, confidence, portion_amount, portion_unit, calories, protein, carbs, fat, fiber, category)
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)`,
          [
            mealId,
            food.name,
            food.confidence ?? 1.0,
            food.portion.amount,
            food.portion.unit,
            food.nutrition.calories,
            food.nutrition.protein,
            food.nutrition.carbs,
            food.nutrition.fat,
            food.nutrition.fiber ?? null,
            food.category
          ]
        );
      }

      const updated = result.rows[0];
      const healthScore = updated.health_score !== null && updated.health_score !== undefined
        ? parseFloat(updated.health_score)
        : computedHealth;

      res.json({
        success: true,
        meal: {
          mealId: updated.id,
          mealType: updated.meal_type,
          imageUrl: updated.image_url,
          totalCalories: updated.total_calories,
          totalProtein: updated.total_protein !== null && updated.total_protein !== undefined ? parseFloat(updated.total_protein) : null,
          totalCarbs: updated.total_carbs !== null && updated.total_carbs !== undefined ? parseFloat(updated.total_carbs) : null,
          totalFat: updated.total_fat !== null && updated.total_fat !== undefined ? parseFloat(updated.total_fat) : null,
          totalFiber: updated.total_fiber !== null && updated.total_fiber !== undefined ? parseFloat(updated.total_fiber) : null,
          healthScore,
          advice,
          notes: updated.notes,
          timestamp: updated.consumed_at,
        },
      });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({
          error: 'Validación fallida',
          details: error.issues,
        });
      }
      next(error);
    }
  }

  async deleteMeal(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { mealId } = req.params;

      // Obtener URL de imagen antes de eliminar
      const mealResult = await pool.query(
        'SELECT image_url FROM meals WHERE id = $1 AND user_id = $2',
        [mealId, userId]
      );

      if (mealResult.rows.length === 0) {
        return res.status(404).json({ error: 'Comida no encontrada' });
      }

      // Eliminar imagen del storage
      await this.storageService.deleteImage(mealResult.rows[0].image_url);

      // Eliminar meal (cascade eliminará detected_foods)
      await pool.query(
        'DELETE FROM meals WHERE id = $1 AND user_id = $2',
        [mealId, userId]
      );

      logger.info(`Comida eliminada: ${mealId}`);

      res.json({ success: true, message: 'Comida eliminada exitosamente' });
    } catch (error) {
      next(error);
    }
  }

  async analyzeTextDescription(req: Request, res: Response, next: NextFunction) {
    const client = await pool.connect();
    let transactionStarted = false;
    
    try {
      const userId = (req as any).user?.id;
      const validatedData = AnalyzeTextSchema.parse(req.body);
      const { description, mealType, timestamp } = validatedData;

      const likely = isLikelyMealDescription(description);
      if (!likely.ok) {
        throw new HttpError(
          400,
          `${likely.reason} Ejemplo: "2 huevos y una tostada".`,
          'INVALID_MEAL_DESCRIPTION',
          { field: 'description' }
        );
      }

      logger.info(`Analizando descripción de texto para usuario: ${userId}`);

      // Analizar descripción con IA
      const analysis = await this.visionService.analyzeTextDescription(description);

      if (!analysis.foods || analysis.foods.length === 0) {
        throw new HttpError(
          422,
          'No pude detectar alimentos en el texto. Prueba a describir la comida con más detalle.',
          'MEAL_NOT_DETECTED',
          { inputType: 'text' }
        );
      }

      // Para análisis de texto, no hay imagen, usaremos un placeholder o null
      const imageUrl = null;

      // Iniciar transacción
      await client.query('BEGIN');
      transactionStarted = true;

      let consumedAt = new Date();
      if (timestamp) {
        if (/^\d+$/.test(timestamp)) {
          consumedAt = new Date(parseInt(timestamp));
        } else {
          consumedAt = new Date(timestamp);
        }
      }
      
      if (isNaN(consumedAt.getTime())) {
        logger.warn(`Timestamp inválido recibido: ${timestamp}, usando fecha actual`);
        consumedAt = new Date();
      }

      const mealDate = consumedAt.toISOString().split('T')[0];

      const sanitizeMealType = (type: string) => this.normalizeMealType(type);

      const healthScore = this.ensureHealthScore(analysis);
      if (!analysis.mealContext) {
        analysis.mealContext = { estimatedMealType: 'snack', portionSize: 'medium', healthScore };
      } else {
        analysis.mealContext.healthScore = healthScore;
      }

      // Insertar meal
      const mealResult = await client.query(
        `INSERT INTO meals (
          user_id, meal_type, image_url, total_calories, total_protein, 
          total_carbs, total_fat, total_fiber, health_score, meal_date, consumed_at
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
        RETURNING *`,
        [
          userId,
          sanitizeMealType(mealType || analysis.mealContext.estimatedMealType),
          imageUrl,
          analysis.totalNutrition.calories,
          analysis.totalNutrition.protein,
          analysis.totalNutrition.carbs,
          analysis.totalNutrition.fat,
          analysis.totalNutrition.fiber || null,
          healthScore,
          mealDate,
          consumedAt,
        ]
      );

      const meal = mealResult.rows[0];

      const validCategories = ['protein', 'carb', 'vegetable', 'fruit', 'dairy', 'fat', 'mixed'];
      const sanitizeCategory = (category: string) => {
        const lowerCat = (category || 'mixed').toLowerCase();
        return validCategories.includes(lowerCat) ? lowerCat : 'mixed';
      };
      const clampConfidence = (conf: number) => Math.max(0, Math.min(1, conf));
      const clampPortion = (amount: number) => Math.max(0, Math.min(9999.99, amount));

      // Insertar detected foods
      const foodInsertPromises = analysis.foods.map((food) =>
        client.query(
          `INSERT INTO detected_foods (
            meal_id, name, confidence, portion_amount, portion_unit,
            calories, protein, carbs, fat, fiber, category
          ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
          RETURNING *`,
          [
            meal.id,
            food.name,
            clampConfidence(food.confidence),
            clampPortion(food.portion.amount),
            food.portion.unit,
            food.nutrition.calories,
            food.nutrition.protein,
            food.nutrition.carbs,
            food.nutrition.fat,
            food.nutrition.fiber || null,
            sanitizeCategory(food.category),
          ]
        )
      );

      const foodResults = await Promise.all(foodInsertPromises);
      const detectedFoods = foodResults.map((r) => r.rows[0]);

      await client.query('COMMIT');

      logger.info(`Descripción de texto analizada exitosamente: ${meal.id}`);

      res.status(201).json({
        mealId: meal.id,
        detectedFoods: detectedFoods.map((food) => ({
          name: food.name,
          confidence: parseFloat(food.confidence),
          portion: {
            amount: parseFloat(food.portion_amount),
            unit: food.portion_unit,
          },
          nutrition: {
            calories: food.calories,
            protein: parseFloat(food.protein),
            carbs: parseFloat(food.carbs),
            fat: parseFloat(food.fat),
            fiber: food.fiber ? parseFloat(food.fiber) : undefined,
          },
          category: food.category,
        })),
        totalNutrition: {
          calories: meal.total_calories,
          protein: parseFloat(meal.total_protein),
          carbs: parseFloat(meal.total_carbs),
          fat: parseFloat(meal.total_fat),
          fiber: meal.total_fiber ? parseFloat(meal.total_fiber) : undefined,
        },
        imageUrl: meal.image_url,
        timestamp: meal.consumed_at,
        mealContext: analysis.mealContext,
        notes: analysis.notes,
      });
    } catch (error) {
      if (transactionStarted) {
        await client.query('ROLLBACK').catch(() => undefined);
      }
      logger.error('Error analizando descripción de texto:', error);
      
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          code: 'VALIDATION_FAILED',
          details: error.issues 
        });
      }
      
      next(error);
    } finally {
      client.release();
    }
  }
}
