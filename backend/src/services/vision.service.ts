// src/services/vision.service.ts
import { GoogleGenerativeAI } from '@google/generative-ai';
import fs from 'fs/promises';
import crypto from 'crypto';
// Simple in-memory cache (puede migrarse a Redis para producción)
const imageAnalysisCache = new Map<string, VisionAnalysisResult>();
import path from 'path';
import { logger } from '../utils/logger';
import { VisionAnalysisResult } from '../types';
import { config } from '../config/env';
import { HttpError } from '../utils/httpError';
import { sanitizeChatMealData, sanitizeVisionAnalysisResult } from '../utils/llmSanitizers';

const VISION_SYSTEM_PROMPT = `
ACTÚA COMO: Nutricionista experto con especialización en visión por computadora.

TU OBJETIVO: Analizar imágenes de alimentos para extraer datos nutricionales precisos en formato JSON estricto.

---
PASOS DE RAZONAMIENTO (Proceso interno):
1.  Escanea la imagen completa para detectar si hay comida. Si NO hay comida, aborta inmediatamente.
2.  Identifica cada componente individual (ej. no solo "ensalada", sino "lechuga", "tomate", "aderezo", "pollo").
3.  Estima el volumen visual comparado con estándares (tazas, puños, tamaño de plato).
4.  Asigna la categoría correcta.
5.  Calcula macros basados en tu base de datos interna y la referencia proporcionada.

---
REGLAS CRÍTICAS:
1.  **Salida JSON Pura**: No incluyas bloques de código \`\`\`json, ni saludos. Solo el objeto JSON.
2.  **Idioma**: Los valores de texto (nombre, notas) deben estar en **ESPAÑOL**. Las claves del JSON en **INGLÉS**.
3.  **Seguridad**: Si la imagen es borrosa, oscura, o no es comida, usa el campo "error".
4.  **Matemáticas**: Asegúrate de que la suma de los items coincida aproximadamente con el total.

---
BASE DE DATOS DE REFERENCIA (Calibración por 100g):
- Arroz blanco: 130 cal | Pollo pechuga: 165 cal | Aguacate: 160 cal
- Huevo (unidad): 70 cal | Pan integral (rebanada): 70 cal | Aceite de oliva (cucharada): 120 cal

---
ESTRUCTURA DE RESPUESTA ESPERADA (JSON):

{
  "is_food": boolean, // true si hay comida, false si es un zapato, persona, etc.
  "error": string | null, // Mensaje amigable si is_food es false, o null
  "reasoning": "Breve explicación de cómo identificaste los alimentos y sus tamaños (ej: 'Veo un filete del tamaño de la palma de la mano...')",
  "foods": [
    {
      "name": "string (Español)",
      "detected_ingredients": ["string"], // Lista de ingredientes visibles si es un plato compuesto
      "portion_display": "string (ej: 1 taza, 150g)", 
      "portion_grams": number, // Estimación en gramos
      "nutrition": {
        "calories": number,
        "protein": number,
        "carbs": number,
        "fat": number,
        "fiber": number
      },
      "category": "protein" | "carb" | "vegetable" | "fruit" | "dairy" | "fat" | "mixed" | "beverage",
      "confidence": number (0.0-1.0)
    }
  ],
  "meal_analysis": {
    "health_score": number (0-100),
    "health_feedback": "Consejo breve en Español (ej: 'Falta proteína, intenta añadir pollo')",
    "dominant_macro": "string"
  }
}
`;

export class VisionService {
  private genAI: GoogleGenerativeAI;

  constructor() {
    this.genAI = new GoogleGenerativeAI(config.ai.geminiApiKey);
  }

  async analyzeMealImage(imagePath: string): Promise<VisionAnalysisResult> {
    const MAX_RETRIES = 2;
    const TIMEOUT_MS = 20000; // 20 segundos
    let lastError: any = null;
    // Leer buffer de imagen de forma segura
    const path = require('path');
    const uploadsRoot = path.resolve(process.env.UPLOAD_PATH || './uploads');
    const absoluteImagePath = path.resolve(imagePath);
    let imageBuffer;
    if (absoluteImagePath.startsWith(uploadsRoot + path.sep)) {
      imageBuffer = await fs.readFile(absoluteImagePath);
    } else {
      throw new Error('Intento de leer archivo fuera de uploads bloqueado: ' + imagePath);
    }
    const hash = crypto.createHash('sha256').update(imageBuffer).digest('hex');
    if (imageAnalysisCache.has(hash)) {
      logger.info(`[Vision] Cache hit para imagen: ${hash}`);
      return imageAnalysisCache.get(hash)!;
    }
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        logger.info(`[Vision] Intento ${attempt + 1} de análisis de imagen`);
        const base64Image = imageBuffer.toString('base64');
        // Determinar el tipo MIME
        const ext = path.extname(imagePath).toLowerCase();
        const mediaType = ext === '.png' ? 'image/png' : 'image/jpeg';
        const model = this.genAI.getGenerativeModel({ model: 'gemini-3-flash-preview' });
        const prompt = `${VISION_SYSTEM_PROMPT}\n\nAnaliza esta imagen de comida y proporciona información nutricional siguiendo el formato especificado.`;
        const imagePart = {
          inlineData: {
            data: base64Image,
            mimeType: mediaType,
          },
        };
        // Timeout wrapper
        const geminiResult = await Promise.race([
          model.generateContent([prompt, imagePart]),
          new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout del modelo LLM')), TIMEOUT_MS))
        ]);
        // @ts-ignore
        const response = geminiResult.response;
        const responseText = response.text();
        logger.info(`[Vision] Respuesta del modelo recibida (longitud: ${responseText?.length || 0})`);
        if (!responseText) {
          throw new Error('No se recibió respuesta del modelo');
        }
        // Parsear JSON
        let jsonStr = responseText;
        const jsonMatch = responseText.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          jsonStr = jsonMatch[0];
        }
        let analysisResult: VisionAnalysisResult;
        try {
          analysisResult = JSON.parse(jsonStr);
        } catch (e) {
          logger.error('Error parseando JSON:', { text: responseText, error: e });
          throw new HttpError(
            502,
            'No pude interpretar la respuesta del modelo. Intenta nuevamente.',
            'LLM_RESPONSE_INVALID',
            { kind: 'json_parse' }
          );
        }
        const sanitized = sanitizeVisionAnalysisResult(analysisResult);
        imageAnalysisCache.set(hash, sanitized);
        return sanitized;
      } catch (error) {
        lastError = error;
        logger.warn(`[Vision] Error en intento ${attempt + 1}:`, error);
        // Si es HttpError, no reintentar
        if (error instanceof HttpError) throw error;
        // Si es el último intento, lanzar error controlado
        if (attempt === MAX_RETRIES) {
          logger.error('Error en análisis de visión tras reintentos:', error);
          throw new HttpError(502, 'Error al analizar la imagen de comida', 'LLM_ANALYSIS_FAILED');
        }
        // Esperar antes de reintentar
        await new Promise((res) => setTimeout(res, 1000));
      }
    }
    // Fallback (no debería llegar aquí)
    throw lastError || new Error('Error desconocido en análisis de imagen');
  }

  async analyzeTextDescription(description: string): Promise<VisionAnalysisResult> {
    const MAX_RETRIES = 2;
    const TIMEOUT_MS = 20000;
    let lastError: any = null;
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        logger.info(`[Vision] Intento ${attempt + 1} de análisis de texto`);
        const TEXT_ANALYSIS_PROMPT = `${VISION_SYSTEM_PROMPT}

DESCRIPCIÓN DE COMIDA: "${description}"

Analiza esta descripción de comida y proporciona información nutricional siguiendo el formato especificado. Estima cantidades razonables basándote en porciones típicas.`;
        const model = this.genAI.getGenerativeModel({ model: 'gemini-3-flash-preview' });
        const geminiResult = await Promise.race([
          model.generateContent(TEXT_ANALYSIS_PROMPT),
          new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout del modelo LLM')), TIMEOUT_MS))
        ]);
        // @ts-ignore
        const response = geminiResult.response;
        const responseText = response.text();
        logger.info(`[Vision] Respuesta del modelo recibida (texto, longitud: ${responseText?.length || 0})`);
        if (!responseText) {
          throw new Error('No se recibió respuesta del modelo');
        }
        // Parsear JSON
        let jsonStr = responseText;
        const jsonMatch = responseText.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          jsonStr = jsonMatch[0];
        }
        let analysisResult: VisionAnalysisResult;
        try {
          analysisResult = JSON.parse(jsonStr);
        } catch (e) {
          logger.error('Error parseando JSON:', { text: responseText, error: e });
          throw new HttpError(
            502,
            'No pude interpretar la respuesta del modelo. Intenta reformular tu comida.',
            'LLM_RESPONSE_INVALID',
            { kind: 'json_parse' }
          );
        }
        return sanitizeVisionAnalysisResult(analysisResult);
      } catch (error) {
        lastError = error;
        logger.warn(`[Vision] Error en intento ${attempt + 1} (texto):`, error);
        if (error instanceof HttpError) throw error;
        if (attempt === MAX_RETRIES) {
          logger.error('Error en análisis de texto tras reintentos:', error);
          throw new HttpError(502, 'Error al analizar la descripción de comida', 'LLM_ANALYSIS_FAILED');
        }
        await new Promise((res) => setTimeout(res, 1000));
      }
    }
    throw lastError || new Error('Error desconocido en análisis de texto');
  }

  async chatNutrition(message: string, conversationHistory?: any[]): Promise<{ message: string; shouldRegisterMeal: boolean; mealData?: any }> {
    try {
      // Nuevo prompt: solo asesoramiento, nunca registrar comidas
      const CHAT_SYSTEM_PROMPT = `Eres un asistente nutricional amigable y experto. 

Tus responsabilidades:
1. Responder preguntas sobre nutrición, dietas y salud
2. Ofrecer consejos personalizados sobre alimentación
3. Ayudar a interpretar información nutricional

No debes registrar comidas ni pedir al usuario que registre comidas. Si el usuario menciona que comió algo, solo ofrece asesoramiento o comentarios, pero nunca digas que has registrado la comida ni devuelvas datos de comidas.`;

      const model = this.genAI.getGenerativeModel({ model: 'gemini-3-flash-preview' });

      // Construir historial de conversación
      let conversationContext = '';
      if (conversationHistory && conversationHistory.length > 0) {
        conversationContext = '\n\nHistorial de conversación:\n';
        conversationHistory.forEach((msg: any) => {
          conversationContext += `${msg.role === 'user' ? 'Usuario' : 'Asistente'}: ${msg.content}\n`;
        });
      }

      const fullPrompt = `${CHAT_SYSTEM_PROMPT}${conversationContext}\n\nMensaje del usuario: "${message}"\n\nResponde solo con un JSON: { "message": "respuesta" }.`;

      const geminiResult = await model.generateContent(fullPrompt);
      const response = await geminiResult.response;
      const responseText = response.text();
      
      if (!responseText) {
        throw new Error('No se recibió respuesta del modelo');
      }

      // Parsear JSON
      let jsonStr = responseText;
      const jsonMatch = responseText.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        jsonStr = jsonMatch[0];
      }
      
      let chatResult: any;
      try {
        chatResult = JSON.parse(jsonStr);
      } catch (e) {
        logger.error('Error parseando JSON del chat:', { text: responseText, error: e });
        // Respuesta por defecto si falla el parseo
        return {
          message: responseText || 'Lo siento, no pude procesar tu mensaje correctamente.',
          shouldRegisterMeal: false,
          mealData: undefined
        };
      }

      return {
        message: chatResult.message || responseText,
        shouldRegisterMeal: false,
        mealData: undefined
      };
    } catch (error) {
      logger.error('Error en chat de nutrición:', error);
      throw new Error('Error al procesar el mensaje');
    }
  }
}
