// src/middleware/rateLimiter.ts
import rateLimit from 'express-rate-limit';


const isProd = process.env.NODE_ENV === 'production';
export const analysisLimiter = rateLimit({
  windowMs: isProd ? 10 * 60 * 1000 : 15 * 60 * 1000, // 10 min prod, 15 min dev
  max: isProd ? 40 : 100, // 40 análisis por 10 min en prod, 100 en dev
  message: 'Demasiadas solicitudes de análisis. Por favor intenta más tarde.',
  standardHeaders: true,
  legacyHeaders: false,
});

export const authLimiter = rateLimit({
  windowMs: isProd ? 15 * 60 * 1000 : 15 * 60 * 1000,
  max: isProd ? 10 : 20, // 10 intentos en prod, 20 en dev
  message: 'Demasiados intentos de inicio de sesión. Por favor intenta más tarde.',
  skipSuccessfulRequests: true,
});
