// src/models/DeviceToken.ts
import { pool } from '../config/database';

export interface DeviceTokenRow {
  id: string;
  user_id: string;
  fcm_token: string;
  platform: string;
  created_at: Date;
  updated_at: Date;
}

export class DeviceTokenModel {
  static async upsert(userId: string, fcmToken: string, platform: string): Promise<DeviceTokenRow> {
    const result = await pool.query(
      `INSERT INTO device_tokens (user_id, fcm_token, platform)
       VALUES ($1, $2, $3)
       ON CONFLICT (fcm_token)
       DO UPDATE SET user_id = $1, platform = $3, updated_at = CURRENT_TIMESTAMP
       RETURNING *`,
      [userId, fcmToken, platform]
    );
    return result.rows[0];
  }

  static async findByUser(userId: string): Promise<DeviceTokenRow[]> {
    const result = await pool.query(
      `SELECT * FROM device_tokens WHERE user_id = $1`,
      [userId]
    );
    return result.rows;
  }

  static async deleteToken(fcmToken: string): Promise<void> {
    await pool.query(`DELETE FROM device_tokens WHERE fcm_token = $1`, [fcmToken]);
  }

  static async deleteTokens(fcmTokens: string[]): Promise<void> {
    if (fcmTokens.length === 0) return;
    await pool.query(`DELETE FROM device_tokens WHERE fcm_token = ANY($1)`, [fcmTokens]);
  }

  static async findUsersWithTokens(): Promise<string[]> {
    const result = await pool.query(`SELECT DISTINCT user_id FROM device_tokens`);
    return result.rows.map((r) => r.user_id);
  }
}
