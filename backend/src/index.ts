// src/index.ts
import dotenv from 'dotenv';
if (process.env.NODE_ENV === 'development') {
  dotenv.config({ path: '.env.local' });
} else {
  dotenv.config();
}

import { validateConfig } from './config/env';
import { createApp } from './app';
import { pool } from './config/database';
import { logger } from './utils/logger';

const PORT = parseInt(process.env.PORT || '3000', 10);

async function startServer() {
  try {
    // VALIDAR CONFIGURACIÓN PRIMERO
    validateConfig();

    // Verificar conexión a base de datos
    const client = await pool.connect();
    logger.info('✅ Conexión a PostgreSQL exitosa');
    client.release();

    // Crear y configurar app
    const app = createApp();

    // Iniciar servidor
    app.listen(PORT, '0.0.0.0', () => {
      logger.info(`🚀 Servidor corriendo en puerto ${PORT} (0.0.0.0)`);
      logger.info(`📊 Environment: ${process.env.NODE_ENV || 'development'}`);
      logger.info(`🔗 Health check: http://localhost:${PORT}/health`);
    });
  } catch (error) {
    logger.error('❌ Error al iniciar servidor:', error);
    process.exit(1);
  }
}

// Manejar errores no capturados
process.on('unhandledRejection', (reason, promise) => {
  logger.error('Unhandled Rejection at:', promise, 'reason:', reason);
  process.exit(1);
});

process.on('uncaughtException', (error) => {
  logger.error('Uncaught Exception:', error);
  process.exit(1);
});

// Iniciar
startServer();
