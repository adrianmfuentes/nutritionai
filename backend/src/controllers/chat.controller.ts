// src/controllers/chat.controller.ts
import { Request, Response, NextFunction } from 'express';
import { VisionService } from '../services/vision.service';
import { logger } from '../utils/logger';
import { z } from 'zod';

const ChatSchema = z.object({
  message: z.string().min(1, 'El mensaje es requerido'),
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
      const { message, conversationHistory } = validatedData;

      logger.info(`Chat request de usuario: ${userId}`);

      // Procesar chat con IA
      const chatResult = await this.visionService.chatNutrition(message, conversationHistory);

      res.json({
        message: chatResult.message,
        shouldRegisterMeal: chatResult.shouldRegisterMeal,
        mealData: chatResult.mealData || undefined,
      });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          details: error.errors 
        });
      }
      
      logger.error('Error en chat:', error);
      next(error);
    }
  }
}
