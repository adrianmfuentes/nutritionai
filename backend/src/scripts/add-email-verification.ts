// src/scripts/add-email-verification.ts
import { pool } from '../config/database';
import { logger } from '../utils/logger';

async function addEmailVerificationColumns() {
  try {
    logger.info('Agregando columnas de verificación de email...');

    // Agregar columnas si no existen
    await pool.query(`
      ALTER TABLE users
      ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE,
      ADD COLUMN IF NOT EXISTS verification_code VARCHAR(6),
      ADD COLUMN IF NOT EXISTS verification_expires TIMESTAMP WITH TIME ZONE
    `);

    logger.info('Columnas de verificación de email agregadas exitosamente');
  } catch (error) {
    logger.error('Error agregando columnas de verificación de email:', error);
    throw error;
  } finally {
    await pool.end();
  }
}

addEmailVerificationColumns();