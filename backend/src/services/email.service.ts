// src/services/email.service.ts
import nodemailer from 'nodemailer';
import { logger } from '../utils/logger';

export class EmailService {
  private transporter: nodemailer.Transporter;

  constructor() {
    this.transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST || 'smtp.gmail.com',
      port: parseInt(process.env.SMTP_PORT || '587'),
      secure: false, // true for 465, false for other ports
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS,
      },
    });
  }

  async sendVerificationEmail(email: string, code: string): Promise<void> {
    const mailOptions = {
      from: process.env.SMTP_FROM || 'noreply@nutritionapp.com',
      to: email,
      subject: 'Verifica tu email - Nutrition App',
      html: `
        <h1>Bienvenido a Nutrition App</h1>
        <p>Tu código de verificación es:</p>
        <h2 style="font-size: 24px; font-weight: bold; color: #333;">${code}</h2>
        <p>Este código expirará en 24 horas.</p>
        <p>Si no solicitaste esta verificación, ignora este email.</p>
      `,
    };

    try {
      await this.transporter.sendMail(mailOptions);
      logger.info(`Email de verificación enviado a ${email}`);
    } catch (error) {
      logger.error(`Error enviando email de verificación a ${email}:`, error);
      throw error;
    }
  }
}