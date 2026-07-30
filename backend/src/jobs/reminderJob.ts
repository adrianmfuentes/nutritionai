// src/jobs/reminderJob.ts
import { pool } from '../config/database';
import { NotificationService } from '../services/notification.service';
import { logger } from '../utils/logger';

const notificationService = new NotificationService();

function computeStreak(sortedDescDates: string[]): number {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  let expected = new Date(today);
  expected.setDate(expected.getDate() - 1); // streak counts consecutive days up to yesterday

  let streak = 0;
  for (const dateStr of sortedDescDates) {
    const date = new Date(dateStr);
    date.setHours(0, 0, 0, 0);
    if (date.getTime() === expected.getTime()) {
      streak++;
      expected.setDate(expected.getDate() - 1);
    } else if (date.getTime() < expected.getTime()) {
      break;
    }
  }
  return streak;
}

export async function runDailyReminderJob(): Promise<void> {
  if (!notificationService.isEnabled()) {
    logger.info('Push notifications deshabilitadas (sin credenciales de Firebase); se omite el job de recordatorios.');
    return;
  }

  logger.info('Iniciando job de recordatorios diarios...');

  const usersResult = await pool.query(
    `SELECT DISTINCT u.id
     FROM users u
     JOIN device_tokens dt ON dt.user_id = u.id
     WHERE u.push_reminders_enabled = TRUE`
  );

  let remindersSent = 0;
  for (const user of usersResult.rows) {
    try {
      const loggedToday = await pool.query(
        `SELECT 1 FROM meals WHERE user_id = $1 AND meal_date = CURRENT_DATE LIMIT 1`,
        [user.id]
      );
      if ((loggedToday.rowCount ?? 0) > 0) continue;

      const recentDates = await pool.query(
        `SELECT DISTINCT meal_date FROM meals
         WHERE user_id = $1 AND meal_date < CURRENT_DATE
         ORDER BY meal_date DESC LIMIT 60`,
        [user.id]
      );
      const streak = computeStreak(recentDates.rows.map((r) => r.meal_date));

      const body = streak > 0
        ? `¡Llevás ${streak} días seguidos! No rompas tu racha, registrá tu próxima comida.`
        : 'Todavía no registraste ninguna comida hoy. Un registro rápido te ayuda a mantener tus objetivos.';

      await notificationService.sendToUser(user.id, {
        title: streak > 0 ? `🔥 Racha de ${streak} días` : '🍽️ ¿Qué comiste hoy?',
        body,
        data: { type: 'daily_reminder', streak: String(streak) },
      });
      remindersSent++;
    } catch (error) {
      logger.error(`Error enviando recordatorio a usuario ${user.id}:`, error);
    }
  }

  logger.info(`Job de recordatorios completado. Enviados: ${remindersSent}/${usersResult.rows.length}`);
}
