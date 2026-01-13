// src/controllers/meals.controller.ts
import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/database';
import { VisionService } from '../services/vision.service';
import { StorageService } from '../services/storage.service';
import { logger } from '../utils/logger';
import { z } from 'zod';

const AnalyzeMealSchema = z.object({
  mealType: z.enum(['breakfast', 'lunch', 'dinner', 'snack']).optional(),
  timestamp: z.string().optional(),
});

const AnalyzeTextSchema = z.object({
  description: z.string().min(1, 'La descripción es requerida'),
  mealType: z.enum(['breakfast', 'lunch', 'dinner', 'snack']).optional(),
  timestamp: z.string().optional(),
});

export class MealsController {
  private visionService: VisionService;
  private storageService: StorageService;

  constructor() {
    this.visionService = new VisionService();
    this.storageService = new StorageService();
  }

  async analyzeMeal(req: Request, res: Response, next: NextFunction) {
    const client = await pool.connect();
    
    try {
      const userId = (req as any).user?.id;
      
      if (!req.file) {
        return res.status(400).json({ error: 'No se proporcionó imagen' });
      }

      const validatedData = AnalyzeMealSchema.parse(req.body);
      const { mealType, timestamp } = validatedData;

      logger.info(`Analizando comida para usuario: ${userId}`);

      // Analizar imagen con IA
      const analysis = await this.visionService.analyzeMealImage(req.file.path);

      // Guardar imagen permanentemente
      const imageUrl = await this.storageService.saveImage(req.file.path, userId);

      // Iniciar transacción
      await client.query('BEGIN');

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
      const validMealTypes = ['breakfast', 'lunch', 'dinner', 'snack'];
      const sanitizeMealType = (type: string) => {
        const lowerType = (type || '').toLowerCase();
        return validMealTypes.includes(lowerType) ? lowerType : 'snack';
      };

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
          analysis.mealContext.healthScore,
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
      await client.query('ROLLBACK');
      logger.error('Error analizando comida:', error);
      
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          details: error.errors 
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
      const meals = result.rows.map(meal => ({
        mealId: meal.id,
        mealType: meal.meal_type,
        imageUrl: meal.image_url,
        totalCalories: meal.total_calories,
        timestamp: meal.consumed_at,
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
      const { notes } = req.body;

      const result = await pool.query(
        `UPDATE meals 
         SET notes = $1, updated_at = CURRENT_TIMESTAMP 
         WHERE id = $2 AND user_id = $3 
         RETURNING *`,
        [notes, mealId, userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ error: 'Comida no encontrada' });
      }

      res.json({ meal: result.rows[0] });
    } catch (error) {
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
    
    try {
      const userId = (req as any).user?.id;
      const validatedData = AnalyzeTextSchema.parse(req.body);
      const { description, mealType, timestamp } = validatedData;

      logger.info(`Analizando descripción de texto para usuario: ${userId}`);

      // Analizar descripción con IA
      const analysis = await this.visionService.analyzeTextDescription(description);

      // Para análisis de texto, no hay imagen, usaremos un placeholder o null
      const imageUrl = null;

      // Iniciar transacción
      await client.query('BEGIN');

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

      const validMealTypes = ['breakfast', 'lunch', 'dinner', 'snack'];
      const sanitizeMealType = (type: string) => {
        const lowerType = (type || '').toLowerCase();
        return validMealTypes.includes(lowerType) ? lowerType : 'snack';
      };

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
          analysis.mealContext.healthScore,
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
      await client.query('ROLLBACK');
      logger.error('Error analizando descripción de texto:', error);
      
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          details: error.errors 
        });
      }
      
      next(error);
    } finally {
      client.release();
    }
  }
}
