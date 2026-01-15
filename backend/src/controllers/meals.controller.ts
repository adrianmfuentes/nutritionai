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

const UpdateMealSchema = z.object({
  notes: z.string().nullable().optional(),
  mealType: z.string().nullable().optional(),
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
    const calories = Number(totals.calories ?? 0);
    const protein = Number(totals.protein ?? 0);
    const fat = Number(totals.fat ?? 0);
    const fiber = Number(totals.fiber ?? 0);

    const proteinScore = Math.min(3, (protein / 30) * 3);
    const fiberScore = Math.min(2, (fiber / 10) * 2);
    const fatPenalty = Math.min(2, (fat / 25) * 2);
    const caloriePenalty = calories > 800 ? Math.min(2, ((calories - 800) / 800) * 2) : 0;

    const score = 5 + proteinScore + fiberScore - fatPenalty - caloriePenalty;
    return Math.max(1, Math.min(10, Number.isFinite(score) ? score : 5));
  }

  private ensureHealthScore(analysis: any): number {
    const raw = analysis?.mealContext?.healthScore;
    if (typeof raw === 'number' && !isNaN(raw)) {
      return Math.max(1, Math.min(10, raw));
    }
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
      // Eliminar imagen temporal después de guardar
      await fs.unlink(imagePath).catch(() => undefined);
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
        await fs.unlink(imagePath).catch(() => undefined);
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

      const existingResult = await pool.query(
        'SELECT total_calories, total_protein, total_carbs, total_fat, total_fiber, health_score FROM meals WHERE id = $1 AND user_id = $2',
        [mealId, userId]
      );

      if (existingResult.rows.length === 0) {
        return res.status(404).json({ error: 'Comida no encontrada' });
      }

      const existing = existingResult.rows[0];
      const computedHealth = (existing.health_score !== null && existing.health_score !== undefined)
        ? parseFloat(existing.health_score)
        : this.computeHealthScoreFromTotals({
            calories: existing.total_calories,
            protein: existing.total_protein,
            carbs: existing.total_carbs,
            fat: existing.total_fat,
            fiber: existing.total_fiber,
          });

      const result = await pool.query(
        `UPDATE meals 
         SET notes = $1, meal_type = COALESCE($2, meal_type), health_score = COALESCE(health_score, $3), updated_at = CURRENT_TIMESTAMP 
         WHERE id = $4 AND user_id = $5 
         RETURNING *`,
        [notes, mealType ? this.normalizeMealType(mealType) : null, computedHealth, mealId, userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ error: 'Comida no encontrada' });
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
