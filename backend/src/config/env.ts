// src/config/env.ts
import dotenv from 'dotenv';

dotenv.config();

export const config = {
  nodeEnv: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT || '3000'),
  
  database: {
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '5432'),
    name: process.env.DB_NAME || 'nutrition_ai',
    user: process.env.DB_USER || 'nutrition_user',
    password: process.env.DB_PASSWORD || '',
  },
  
  jwt: {
    secret: process.env.JWT_SECRET || 'default-secret-change-in-production',
    accessExpiresIn: process.env.JWT_ACCESS_EXPIRES_IN || '15m',
    refreshExpiresDays: parseInt(process.env.JWT_REFRESH_EXPIRES_DAYS || '30'),
  },
  
  ai: {
    geminiApiKey: process.env.GEMINI_API_KEY || '',
  },
  
  storage: {
    uploadPath: process.env.UPLOAD_PATH || './uploads',
    maxFileSize: parseInt(process.env.MAX_FILE_SIZE || '10485760'), // 10MB
  },
  
  rateLimit: {
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000'), // 15 min
    maxRequests: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100'),
  },
  
  cors: {
    origin: process.env.CORS_ORIGIN || '*',
  },
  
  logging: {
    level: process.env.LOG_LEVEL || 'info',
    path: process.env.LOG_PATH || './logs',
  },
};

import { logger } from '../utils/logger';

export function validateConfig() {
  const errors: string[] = [];

  if (!config.ai.geminiApiKey) {
    errors.push('Falta GEMINI_API_KEY o ANTHROPIC_API_KEY');
  }

  if (!config.database.host) errors.push('Falta DB_HOST');
  if (!config.database.name) errors.push('Falta DB_NAME');
  if (!config.database.user) errors.push('Falta DB_USER');
  if (!config.database.password) errors.push('Falta DB_PASSWORD');
  if (!config.jwt.secret) errors.push('Falta JWT_SECRET');

  if (errors.length > 0) {
    logger.error('❌ Errores de configuración:', errors);
    throw new Error(`Configuración inválida: ${errors.join(', ')}`);
  }

  logger.info('✅ Configuración validada correctamente');
  logger.info(`🤖 Proveedor de IA: ${config.ai.geminiApiKey ? 'Google Gemini' : 'Anthropic Claude'}`);
  // No se loguea ninguna parte de la API Key por seguridad
}