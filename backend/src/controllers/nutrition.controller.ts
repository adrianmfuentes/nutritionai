// src/controllers/nutrition.controller.ts
import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/database';
import { logger } from '../utils/logger';

export class NutritionController {
  async getDailySummary(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { date } = req.query;

      const targetDate = date || new Date().toISOString().split('T')[0];

      // Obtener totales del día
      const mealsResult = await pool.query(
        `SELECT 
          COALESCE(SUM(total_calories), 0) as calories,
          COALESCE(SUM(total_protein), 0) as protein,
          COALESCE(SUM(total_carbs), 0) as carbs,
          COALESCE(SUM(total_fat), 0) as fat,
          COALESCE(SUM(total_fiber), 0) as fiber,
          COUNT(*) as meal_count
         FROM meals
         WHERE user_id = $1 AND meal_date = $2`,
        [userId, targetDate]
      );

      const totals = mealsResult.rows[0];

      // Obtener goals del usuario
      const goalsResult = await pool.query(
        `SELECT daily_calories, daily_protein, daily_carbs, daily_fat
         FROM nutrition_goals
         WHERE user_id = $1 AND active_from <= $2
         ORDER BY active_from DESC
         LIMIT 1`,
        [userId, targetDate]
      );

      const goals = goalsResult.rows[0] || {
        daily_calories: 2000,
        daily_protein: 150,
        daily_carbs: 200,
        daily_fat: 65,
      };

      // Calcular progreso
      const progress = {
        caloriesPercent: (parseFloat(totals.calories) / goals.daily_calories) * 100,
        proteinPercent: (parseFloat(totals.protein) / goals.daily_protein) * 100,
        carbsPercent: (parseFloat(totals.carbs) / goals.daily_carbs) * 100,
        fatPercent: (parseFloat(totals.fat) / goals.daily_fat) * 100,
      };

      // Obtener lista de comidas del día
      const mealsListResult = await pool.query(
        `SELECT id, meal_type, total_calories, total_protein, total_carbs, total_fat, 
                consumed_at, image_url, health_score
         FROM meals
         WHERE user_id = $1 AND meal_date = $2
         ORDER BY consumed_at ASC`,
        [userId, targetDate]
      );

      res.json({
        date: targetDate,
        totals: {
          calories: parseInt(totals.calories),
          protein: parseFloat(totals.protein),
          carbs: parseFloat(totals.carbs),
          fat: parseFloat(totals.fat),
          fiber: parseFloat(totals.fiber),
        },
        goals: {
          calories: goals.daily_calories,
          protein: goals.daily_protein,
          carbs: goals.daily_carbs,
          fat: goals.daily_fat,
        },
        progress: {
          caloriesPercent: Math.round(progress.caloriesPercent),
          proteinPercent: Math.round(progress.proteinPercent),
          carbsPercent: Math.round(progress.carbsPercent),
          fatPercent: Math.round(progress.fatPercent),
        },
        meals: mealsListResult.rows.map(m => ({
          mealId: m.id,
          mealType: m.meal_type,
          imageUrl: m.image_url,
          totalCalories: m.total_calories,
          timestamp: m.consumed_at,
        })),
      });
    } catch (error) {
      next(error);
    }
  }

  async getWeeklySummary(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { startDate } = req.query;

      const start = startDate || new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

      const result = await pool.query(
        `SELECT 
          meal_date,
          SUM(total_calories) as daily_calories,
          SUM(total_protein) as daily_protein,
          SUM(total_carbs) as daily_carbs,
          SUM(total_fat) as daily_fat,
          COUNT(*) as meal_count,
          AVG(health_score) as avg_health_score
         FROM meals
         WHERE user_id = $1 AND meal_date >= $2
         GROUP BY meal_date
         ORDER BY meal_date ASC`,
        [userId, start]
      );

      const days = result.rows;

      // Calcular promedios
      const averages = days.reduce(
        (acc, day) => ({
          calories: acc.calories + parseInt(day.daily_calories),
          protein: acc.protein + parseFloat(day.daily_protein),
          carbs: acc.carbs + parseFloat(day.daily_carbs),
          fat: acc.fat + parseFloat(day.daily_fat),
        }),
        { calories: 0, protein: 0, carbs: 0, fat: 0 }
      );

      const dayCount = days.length || 1;
      Object.keys(averages).forEach((key) => {
        averages[key as keyof typeof averages] = Math.round(averages[key as keyof typeof averages] / dayCount);
      });

      // Determinar tendencia (comparar primera mitad vs segunda mitad)
      const midPoint = Math.floor(dayCount / 2);
      const firstHalf = days.slice(0, midPoint);
      const secondHalf = days.slice(midPoint);

      const firstAvg = firstHalf.reduce((sum, d) => sum + parseInt(d.daily_calories), 0) / (firstHalf.length || 1);
      const secondAvg = secondHalf.reduce((sum, d) => sum + parseInt(d.daily_calories), 0) / (secondHalf.length || 1);

      const trend = secondAvg > firstAvg * 1.05 ? 'increasing' : 
                    secondAvg < firstAvg * 0.95 ? 'decreasing' : 'stable';

      res.json({
        days: days.map(d => ({
          date: d.meal_date,
          totals: {
            calories: parseInt(d.daily_calories),
            protein: parseFloat(d.daily_protein),
            carbs: parseFloat(d.daily_carbs),
            fat: parseFloat(d.daily_fat),
          },
          mealCount: parseInt(d.meal_count),
        })),
        averages,
        trend,
      });
    } catch (error) {
      next(error);
    }
  }

  async updateGoals(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { dailyCalories, proteinGrams, carbsGrams, fatGrams } = req.body;

      // Validar valores
      if (!dailyCalories || !proteinGrams || !carbsGrams || !fatGrams) {
        return res.status(400).json({ error: 'Todos los valores son requeridos' });
      }

      const result = await pool.query(
        `INSERT INTO nutrition_goals (user_id, daily_calories, daily_protein, daily_carbs, daily_fat, active_from)
         VALUES ($1, $2, $3, $4, $5, CURRENT_DATE)
         ON CONFLICT (user_id, active_from) 
         DO UPDATE SET 
           daily_calories = $2,
           daily_protein = $3,
           daily_carbs = $4,
           daily_fat = $5
         RETURNING *`,
        [userId, dailyCalories, proteinGrams, carbsGrams, fatGrams]
      );

      logger.info(`Goals actualizados para usuario: ${userId}`);

      res.json({ 
        goals: {
          calories: dailyCalories,
          protein: proteinGrams,
          carbs: carbsGrams,
          fat: fatGrams
        },
        message: 'Objetivos nutricionales actualizados' 
      });
    } catch (error) {
      next(error);
    }
  }
}
