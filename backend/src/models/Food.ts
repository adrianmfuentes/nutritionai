// src/models/Food.ts
import { pool } from '../config/database';
import { DetectedFood } from '../types';

export class FoodModel {
  static async findByMealId(mealId: string): Promise<DetectedFood[]> {
    const result = await pool.query(
      `SELECT id, meal_id, name, confidence, portion_amount, portion_unit,
              calories, protein, carbs, fat, fiber, category, created_at
       FROM detected_foods 
       WHERE meal_id = $1 
       ORDER BY calories DESC`,
      [mealId]
    );
    return result.rows;
  }

  static async create(mealId: string, food: Omit<DetectedFood, 'id' | 'meal_id' | 'created_at'>): Promise<DetectedFood> {
    const result = await pool.query(
      `INSERT INTO detected_foods (
        meal_id, name, confidence, portion_amount, portion_unit,
        calories, protein, carbs, fat, fiber, category
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
      RETURNING *`,
      [
        mealId,
        food.name,
        food.confidence,
        food.portion_amount,
        food.portion_unit,
        food.calories,
        food.protein,
        food.carbs,
        food.fat,
        food.fiber || null,
        food.category,
      ]
    );
    return result.rows[0];
  }
}
