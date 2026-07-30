// src/services/email.service.ts
import nodemailer from 'nodemailer';
import { logger } from '../utils/logger';
import dotenv from 'dotenv';

// Cargar variables de entorno
if (process.env.NODE_ENV === 'development') {
  dotenv.config({ path: '.env.local' }); // Para desarrollo, usa .env.local
} else {
  dotenv.config(); // Para producción, usa .env
}

export class EmailService {
  private transporter: nodemailer.Transporter;

  constructor() {
    this.transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST || 'smtp-relay.brevo.com',
      port: parseInt(process.env.SMTP_PORT || '587'),
      secure: false,
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS,
      },
    });

    // Verificamos la conexión al iniciar (opcional, ayuda a depurar)
    this.transporter.verify((error) => {
      if (error) {
        logger.warn('⚠️ Error conectando al servidor SMTP:', error);
      } else {
        logger.info('✅ Servidor SMTP listo para enviar correos');
      }
    });
  }

  async sendVerificationEmail(email: string, code: string): Promise<void> {
    const mailOptions = {
      from: process.env.SMTP_FROM || '"Nutrition App" <noreply@nutritionapp.com>',
      to: email,
      subject: 'Verifica tu email - Nutrition App',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h1 style="color: #4A90E2;">Bienvenido a Nutrition App</h1>
          <p>Tu código de verificación es:</p>
          <div style="background-color: #f4f4f4; padding: 15px; text-align: center; border-radius: 5px;">
            <h2 style="font-size: 32px; font-weight: bold; color: #333; margin: 0; letter-spacing: 5px;">${code}</h2>
          </div>
          <p style="margin-top: 20px; color: #666;">Este código expirará en 24 horas.</p>
        </div>
      `,
    };

    try {
      const info = await this.transporter.sendMail(mailOptions);
      logger.info(`Email de verificación enviado a ${email}. ID: ${info.messageId}`);
    } catch (error) {
      logger.error(`Error enviando email SMTP a ${email}:`, error);
      throw error;
    }
  }

  async sendWeeklyDigest(
    email: string,
    name: string,
    stats: {
      avgCalories: number;
      avgProtein: number;
      goalCalories: number;
      daysLogged: number;
      bestHealthScore: number;
    }
  ): Promise<void> {
    const goalPercent = stats.goalCalories > 0
      ? Math.round((stats.avgCalories / stats.goalCalories) * 100)
      : 0;

    const mailOptions = {
      from: process.env.SMTP_FROM || '"Nutrition App" <noreply@nutritionapp.com>',
      to: email,
      subject: 'Tu resumen semanal - Nutrition App',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h1 style="color: #4A90E2;">Hola ${name} 👋</h1>
          <p>Así te fue esta semana con tu seguimiento nutricional:</p>
          <div style="background-color: #f4f4f4; padding: 20px; border-radius: 8px; margin: 16px 0;">
            <p style="margin: 6px 0;"><strong>Días registrados:</strong> ${stats.daysLogged} / 7</p>
            <p style="margin: 6px 0;"><strong>Calorías promedio:</strong> ${stats.avgCalories} kcal (${goalPercent}% de tu objetivo)</p>
            <p style="margin: 6px 0;"><strong>Proteína promedio:</strong> ${stats.avgProtein} g/día</p>
            <p style="margin: 6px 0;"><strong>Mejor puntuación de salud:</strong> ${stats.bestHealthScore}/10</p>
          </div>
          <p style="color: #666;">Seguí registrando tus comidas para mantener el ritmo. ¡Vamos por otra semana!</p>
        </div>
      `,
    };

    try {
      const info = await this.transporter.sendMail(mailOptions);
      logger.info(`Resumen semanal enviado a ${email}. ID: ${info.messageId}`);
    } catch (error) {
      logger.error(`Error enviando resumen semanal a ${email}:`, error);
      throw error;
    }
  }
}