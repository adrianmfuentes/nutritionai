// src/jobs/scheduler.ts
import cron from 'node-cron';
import { logger } from '../utils/logger';
import { runWeeklyDigestJob } from './digestJob';
import { runDailyReminderJob } from './reminderJob';

// Lunes 09:00 UTC por defecto
const DIGEST_CRON = process.env.DIGEST_CRON || '0 9 * * 1';
// Todos los días 20:00 UTC por defecto
const REMINDER_CRON = process.env.REMINDER_CRON || '0 20 * * *';

export function startScheduledJobs(): void {
  cron.schedule(DIGEST_CRON, () => {
    runWeeklyDigestJob().catch((error) => logger.error('Error en job de resumen semanal:', error));
  });

  cron.schedule(REMINDER_CRON, () => {
    runDailyReminderJob().catch((error) => logger.error('Error en job de recordatorios diarios:', error));
  });

  logger.info(`Jobs programados: resumen semanal (${DIGEST_CRON}), recordatorios diarios (${REMINDER_CRON})`);
}
