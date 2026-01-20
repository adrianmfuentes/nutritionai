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
}