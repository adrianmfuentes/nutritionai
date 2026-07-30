// src/controllers/notifications.controller.ts
import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import { pool } from '../config/database';
import { DeviceTokenModel } from '../models/DeviceToken';
import { logger } from '../utils/logger';

const RegisterDeviceSchema = z.object({
  fcmToken: z.string().min(1, 'El token FCM es requerido'),
  platform: z.string().default('android'),
});

const UnregisterDeviceSchema = z.object({
  fcmToken: z.string().min(1, 'El token FCM es requerido'),
});

const PreferencesSchema = z.object({
  weeklyDigestEnabled: z.boolean().optional(),
  pushRemindersEnabled: z.boolean().optional(),
});

export class NotificationsController {
  async registerDevice(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { fcmToken, platform } = RegisterDeviceSchema.parse(req.body);

      await DeviceTokenModel.upsert(userId, fcmToken, platform);
      logger.info(`Dispositivo registrado para push: usuario ${userId}`);

      res.status(201).json({ success: true });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ error: 'Validación fallida', details: error.issues });
      }
      next(error);
    }
  }

  async unregisterDevice(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { fcmToken } = UnregisterDeviceSchema.parse(req.body);

      const owned = await pool.query(
        'SELECT id FROM device_tokens WHERE fcm_token = $1 AND user_id = $2',
        [fcmToken, userId]
      );
      if (owned.rows.length > 0) {
        await DeviceTokenModel.deleteToken(fcmToken);
      }

      res.json({ success: true });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ error: 'Validación fallida', details: error.issues });
      }
      next(error);
    }
  }

  async updatePreferences(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const { weeklyDigestEnabled, pushRemindersEnabled } = PreferencesSchema.parse(req.body);

      const fields: string[] = [];
      const values: any[] = [];
      let idx = 1;

      if (weeklyDigestEnabled !== undefined) {
        fields.push(`weekly_digest_enabled = $${idx++}`);
        values.push(weeklyDigestEnabled);
      }
      if (pushRemindersEnabled !== undefined) {
        fields.push(`push_reminders_enabled = $${idx++}`);
        values.push(pushRemindersEnabled);
      }

      if (fields.length > 0) {
        values.push(userId);
        await pool.query(`UPDATE users SET ${fields.join(', ')} WHERE id = $${idx}`, values);
      }

      res.json({ success: true });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ error: 'Validación fallida', details: error.issues });
      }
      next(error);
    }
  }
}
