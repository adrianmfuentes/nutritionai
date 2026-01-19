// src/models/User.ts
import { pool } from '../config/database';
import { User } from '../types';

export class UserModel {
  static async findById(id: string): Promise<User | null> {
    const result = await pool.query(
      'SELECT id, email, name, profile_photo, created_at, updated_at FROM users WHERE id = $1',
      [id]
    );
    return result.rows[0] || null;
  }

  static async findByEmail(email: string): Promise<User | null> {
    const result = await pool.query(
      'SELECT * FROM users WHERE email = $1',
      [email]
    );
    return result.rows[0] || null;
  }

  static async create(email: string, passwordHash: string, name: string): Promise<User> {
    const result = await pool.query(
      `INSERT INTO users (email, password_hash, name) 
       VALUES ($1, $2, $3) 
       RETURNING id, email, name, profile_photo, created_at, updated_at`,
      [email, passwordHash, name]
    );
    return result.rows[0];
  }

  static async update(id: string, updates: Partial<User>): Promise<User | null> {
    const fields = [];
    const values = [];
    let paramIndex = 1;

    if (updates.name !== undefined) {
      fields.push(`name = $${paramIndex++}`);
      values.push(updates.name);
    }

    if (updates.profile_photo !== undefined) {
      fields.push(`profile_photo = $${paramIndex++}`);
      values.push(updates.profile_photo);
    }

    if (fields.length === 0) return null;

    values.push(id);
    const query = `
      UPDATE users 
      SET ${fields.join(', ')}, updated_at = CURRENT_TIMESTAMP 
      WHERE id = $${paramIndex}
      RETURNING id, email, name, profile_photo, created_at, updated_at
    `;

    const result = await pool.query(query, values);
    return result.rows[0] || null;
  }
}
