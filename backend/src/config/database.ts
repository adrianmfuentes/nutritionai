// src/config/database.ts
import { Pool } from 'pg';
import { logger } from '../utils/logger';

const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME || 'nutrition_ai',
  user: process.env.DB_USER || 'nutrition_user',
  password: process.env.DB_PASSWORD,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

pool.on('connect', () => {
  logger.info('Nueva conexión a PostgreSQL establecida');
});

pool.on('error', (err) => {
  logger.error('Error inesperado en el pool de PostgreSQL:', err);
  process.exit(-1);
});

export { pool };
