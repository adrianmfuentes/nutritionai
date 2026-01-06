// src/services/nutrition.service.ts
import { pool } from '../config/database';
import { logger } from '../utils/logger';

export class NutritionService {
  async getDailySummary(userId: string, date: string) {
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
      [userId, date]
    );

    const totals = mealsResult.rows[0];

    // Obtener goals del usuario
    const goalsResult = await pool.query(
      `SELECT daily_calories, daily_protein, daily_carbs, daily_fat
       FROM nutrition_goals
       WHERE user_id = $1 AND active_from <= $2
       ORDER BY active_from DESC
       LIMIT 1`,
      [userId, date]
    );

    const goals = goalsResult.rows[0] || {
      daily_calories: 2000,
      daily_protein: 150,
      daily_carbs: 200,
      daily_fat: 65,
    };

    return {
      totals: {
        calories: parseInt(totals.calories),
        protein: parseFloat(totals.protein),
        carbs: parseFloat(totals.carbs),
        fat: parseFloat(totals.fat),
        fiber: parseFloat(totals.fiber),
      },
      goals,
      progress: {
        caloriesPercent: Math.round((parseFloat(totals.calories) / goals.daily_calories) * 100),
        proteinPercent: Math.round((parseFloat(totals.protein) / goals.daily_protein) * 100),
        carbsPercent: Math.round((parseFloat(totals.carbs) / goals.daily_carbs) * 100),
        fatPercent: Math.round((parseFloat(totals.fat) / goals.daily_fat) * 100),
      },
    };
  }

  async getWeeklySummary(userId: string, startDate: string) {
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
      [userId, startDate]
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

    // Determinar tendencia
    const midPoint = Math.floor(dayCount / 2);
    const firstHalf = days.slice(0, midPoint);
    const secondHalf = days.slice(midPoint);

    const firstAvg = firstHalf.reduce((sum, d) => sum + parseInt(d.daily_calories), 0) / (firstHalf.length || 1);
    const secondAvg = secondHalf.reduce((sum, d) => sum + parseInt(d.daily_calories), 0) / (secondHalf.length || 1);

    const trend = secondAvg > firstAvg * 1.05 ? 'increasing' : 
                  secondAvg < firstAvg * 0.95 ? 'decreasing' : 'stable';

    return { days, averages, trend };
  }

  async updateGoals(
    userId: string,
    goals: { dailyCalories: number; proteinGrams: number; carbsGrams: number; fatGrams: number }
  ) {
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
      [userId, goals.dailyCalories, goals.proteinGrams, goals.carbsGrams, goals.fatGrams]
    );

    logger.info(`Goals actualizados para usuario: ${userId}`);
    return result.rows[0];
  }
}
