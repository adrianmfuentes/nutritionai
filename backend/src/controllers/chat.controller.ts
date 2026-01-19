// src/controllers/chat.controller.ts
import { Request, Response, NextFunction } from 'express';
import { VisionService } from '../services/vision.service';
import { logger } from '../utils/logger';
import { z } from 'zod';

const SUPPORTED_LANGUAGES = ['es', 'en', 'fr', 'de'] as const;
type SupportedLanguage = typeof SUPPORTED_LANGUAGES[number];

const ChatSchema = z.object({
  message: z.string().min(1, 'El mensaje es requerido'),
  language: z.string().optional(),
  conversationHistory: z.array(z.object({
    role: z.string(),
    content: z.string(),
    timestamp: z.number().optional(),
  })).optional(),
});

export class ChatController {
  private visionService: VisionService;

  constructor() {
    this.visionService = new VisionService();
  }

  async chat(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const validatedData = ChatSchema.parse(req.body);
      const { message, language, conversationHistory } = validatedData;

      // Validar y normalizar idioma
      const userLanguage: SupportedLanguage = this.validateLanguage(language);

      logger.info(`Chat request de usuario: ${userId}, idioma: ${userLanguage}`);

      // Procesar chat con IA
      const chatResult = await this.visionService.chatNutrition(message, conversationHistory);

      res.json({
        message: chatResult.message
      });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          code: 'VALIDATION_FAILED',
          details: error.issues 
        });
      }
      
      logger.error('Error en chat:', error);
      next(error);
    }
  }

  private validateLanguage(language?: string): SupportedLanguage {
    if (!language || !SUPPORTED_LANGUAGES.includes(language as SupportedLanguage)) {
      return 'es'; // Fallback a español
    }
    return language as SupportedLanguage;
  }
}
