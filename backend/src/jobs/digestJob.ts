// src/jobs/digestJob.ts
import { pool } from '../config/database';
import { EmailService } from '../services/email.service';
import { logger } from '../utils/logger';

const emailService = new EmailService();

export async function runWeeklyDigestJob(): Promise<void> {
  logger.info('Iniciando job de resumen semanal...');

  const usersResult = await pool.query(
    `SELECT id, email, name FROM users
     WHERE email_verified = TRUE AND weekly_digest_enabled = TRUE`
  );

  let sent = 0;
  for (const user of usersResult.rows) {
    try {
      const statsResult = await pool.query(
        `SELECT
           COALESCE(AVG(daily_calories), 0) as avg_calories,
           COALESCE(AVG(daily_protein), 0) as avg_protein,
           COALESCE(MAX(daily_health), 0) as best_health_score,
           COUNT(*) as days_logged
         FROM (
           SELECT meal_date,
                  SUM(total_calories) as daily_calories,
                  SUM(total_protein) as daily_protein,
                  MAX(health_score) as daily_health
           FROM meals
           WHERE user_id = $1 AND meal_date >= CURRENT_DATE - INTERVAL '7 days'
           GROUP BY meal_date
         ) daily`,
        [user.id]
      );

      const stats = statsResult.rows[0];
      const daysLogged = parseInt(stats.days_logged);

      // No enviar el resumen si el usuario no registró nada esta semana
      if (daysLogged === 0) continue;

      const goalsResult = await pool.query(
        `SELECT daily_calories FROM nutrition_goals
         WHERE user_id = $1 ORDER BY active_from DESC LIMIT 1`,
        [user.id]
      );
      const goalCalories = goalsResult.rows[0]?.daily_calories ?? 2000;

      await emailService.sendWeeklyDigest(user.email, user.name || 'ahí', {
        avgCalories: Math.round(parseFloat(stats.avg_calories)),
        avgProtein: Math.round(parseFloat(stats.avg_protein)),
        goalCalories: Math.round(goalCalories),
        daysLogged,
        bestHealthScore: Math.round(parseFloat(stats.best_health_score)),
      });
      sent++;
    } catch (error) {
      logger.error(`Error enviando resumen semanal a usuario ${user.id}:`, error);
    }
  }

  logger.info(`Job de resumen semanal completado. Enviados: ${sent}/${usersResult.rows.length}`);
}
