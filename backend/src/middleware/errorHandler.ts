// src/middleware/errorHandler.ts
import { Request, Response, NextFunction } from 'express';
import { logger } from '../utils/logger';
import { HttpError } from '../utils/httpError';

export function errorHandler(
  error: Error,
  req: Request,
  res: Response,
  next: NextFunction
) {
  logger.error('Error:', {
    message: error.message,
    stack: error.stack,
    path: req.path,
    method: req.method,
  });

  // Multer errors
  if (error.message.includes('Tipo de archivo no permitido')) {
    return res.status(400).json({ error: error.message });
  }

  if (error.message.includes('File too large')) {
    return res.status(413).json({ error: 'Archivo demasiado grande. Máximo 10MB' });
  }

  // Database errors
  if (error.message.includes('duplicate key')) {
    return res.status(409).json({ error: 'El recurso ya existe' });
  }

  // Explicit HTTP errors
  if (error instanceof HttpError) {
    return res.status(error.status).json({
      error: error.message,
      code: error.code,
      details: error.details,
    });
  }

  // Default error
  res.status(500).json({
    error: 'Error interno del servidor',
    message: process.env.NODE_ENV === 'development' ? error.message : undefined,
  });
}
