// src/app.ts
import express, { Application } from 'express';
import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';
import { errorHandler } from './middleware/errorHandler';
import authRoutes from './routes/auth.routes';
import mealsRoutes from './routes/meals.routes';
import nutritionRoutes from './routes/nutrition.routes';
import profileRoutes from './routes/profile.routes';
import chatRoutes from './routes/chat.routes';

export function createApp(): Application {
  const app = express();
  app.set('trust proxy', 1);

  // Security middleware
  app.use(helmet());
  app.use(cors({
    origin: process.env.CORS_ORIGIN || '*',
    credentials: true
  }));

  // Rate limiting
  const limiter = rateLimit({
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000'),
    max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100'),
    message: 'Demasiadas peticiones desde esta IP'
  });
  app.use('/v1/', limiter);

  // Body parsing
  app.use(express.json({ limit: '10mb' }));
  app.use(express.urlencoded({ extended: true, limit: '10mb' }));

  // Health check
  app.get('/health', (req, res) => {
    res.status(200).json({ status: 'ok', timestamp: new Date().toISOString() });
  });

  // API Routes
  app.use('/v1/auth', authRoutes);
  app.use('/v1/meals', mealsRoutes);
  app.use('/v1/nutrition', nutritionRoutes);
  app.use('/v1/profile', profileRoutes);
  app.use('/v1/chat', chatRoutes);

  // 404 handler
  app.use((req, res) => {
    res.status(404).json({ error: 'Endpoint no encontrado' });
  });

  // Error handler
  app.use(errorHandler);

  return app;
}
