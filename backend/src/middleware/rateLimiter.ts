// src/middleware/rateLimiter.ts
import rateLimit from 'express-rate-limit';

export const analysisLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutos
  max: 10, // 10 análisis por 15 minutos
  message: 'Demasiadas solicitudes de análisis. Por favor intenta más tarde.',
  standardHeaders: true,
  legacyHeaders: false,
});

export const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5, // 5 intentos de login por 15 minutos
  message: 'Demasiados intentos de inicio de sesión. Por favor intenta más tarde.',
  skipSuccessfulRequests: true,
});
