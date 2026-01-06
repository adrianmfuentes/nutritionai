// src/models/Meal.ts
import { pool } from '../config/database';
import { Meal } from '../types';

export class MealModel {
  static async findById(id: string, userId: string): Promise<Meal | null> {
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
      [id, userId]
    );
    return result.rows[0] || null;
  }

  static async findByUser(
    userId: string,
    options: { date?: string; limit?: number; offset?: number } = {}
  ): Promise<{ meals: Meal[]; total: number }> {
    const { date, limit = 20, offset = 0 } = options;

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
    params.push(limit, offset);

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

    return { meals: result.rows, total };
  }

  static async delete(id: string, userId: string): Promise<boolean> {
    const result = await pool.query(
      'DELETE FROM meals WHERE id = $1 AND user_id = $2 RETURNING id',
      [id, userId]
    );
    return result.rowCount !== null && result.rowCount > 0;
  }
}
