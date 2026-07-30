// src/models/RefreshToken.ts
import { pool } from '../config/database';

export interface RefreshTokenRow {
  id: string;
  user_id: string;
  token_hash: string;
  expires_at: Date;
  revoked_at: Date | null;
  created_at: Date;
}

export class RefreshTokenModel {
  static async create(userId: string, tokenHash: string, expiresAt: Date): Promise<RefreshTokenRow> {
    const result = await pool.query(
      `INSERT INTO refresh_tokens (user_id, token_hash, expires_at)
       VALUES ($1, $2, $3)
       RETURNING *`,
      [userId, tokenHash, expiresAt]
    );
    return result.rows[0];
  }

  static async findValidByHash(tokenHash: string): Promise<RefreshTokenRow | null> {
    const result = await pool.query(
      `SELECT * FROM refresh_tokens
       WHERE token_hash = $1 AND revoked_at IS NULL AND expires_at > CURRENT_TIMESTAMP`,
      [tokenHash]
    );
    return result.rows[0] || null;
  }

  static async revokeByHash(tokenHash: string): Promise<void> {
    await pool.query(
      `UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP WHERE token_hash = $1`,
      [tokenHash]
    );
  }

  static async revokeAllForUser(userId: string): Promise<void> {
    await pool.query(
      `UPDATE refresh_tokens SET revoked_at = CURRENT_TIMESTAMP WHERE user_id = $1 AND revoked_at IS NULL`,
      [userId]
    );
  }
}
