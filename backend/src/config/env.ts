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
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
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
