// src/services/vision.service.ts
import { GoogleGenerativeAI } from '@google/generative-ai';
import fs from 'fs/promises';
import path from 'path';
import { logger } from '../utils/logger';
import { VisionAnalysisResult } from '../types';
import { config } from '../config/env';

const VISION_SYSTEM_PROMPT = `Eres un nutricionista profesional especializado en reconocimiento de alimentos y análisis nutricional.

TAREA: Analiza la imagen de comida proporcionada y devuelve un JSON estructurado con información nutricional detallada.

REQUISITOS DE ANÁLISIS:
1. Identifica TODOS los alimentos visibles en la imagen
2. Estima el tamaño de las porciones basándote en señales visuales (tamaño del plato, comparaciones)
3. Calcula valores precisos de macronutrientes por alimento
4. Usa tamaños de porción estándar (gramos, tazas, piezas) apropiados para cada alimento
5. Proporciona puntajes de confianza para cada identificación

FORMATO DE SALIDA (JSON estricto):
{
  "foods": [
    {
      "name": "nombre del alimento en español",
      "confidence": 0.95,
      "portion": { "amount": 150, "unit": "g" },
      "nutrition": {
        "calories": 250,
        "protein": 20,
        "carbs": 30,
        "fat": 8,
        "fiber": 5
      },
      "category": "protein|carb|vegetable|fruit|dairy|fat|mixed"
    }
  ],
  "totalNutrition": {
    "calories": 250,
    "protein": 20,
    "carbs": 30,
    "fat": 8,
    "fiber": 5
  },
  "mealContext": {
    "estimatedMealType": "breakfast|lunch|dinner|snack",
    "portionSize": "medium",
    "healthScore": 7.5
  },
  "notes": "Observación breve sobre la composición de la comida"
}

REGLAS:
- IMPORTANTE: El campo 'category' DEBE ser uno de: 'protein', 'carb', 'vegetable', 'fruit', 'dairy', 'fat', 'mixed'.
- Devuelve SOLO JSON válido, sin texto adicional
- Todos los valores nutricionales en gramos excepto calorías (kcal)
- Rango de confianza: 0.0 a 1.0
- Si no estás seguro de un elemento, inclúyelo con confianza más baja
- Para platos mixtos, desglosa en componentes cuando sea posible
- Usa unidades métricas (gramos, ml)
- Incluye fibra cuando sea relevante
- Puntuación de salud: 1-10 basada en balance nutricional

BASE DE DATOS DE ALIMENTOS COMUNES (referencia):
- Arroz blanco (100g): 130 cal, 2.7g proteína, 28g carbos, 0.3g grasa
- Pollo pechuga (100g): 165 cal, 31g proteína, 0g carbos, 3.6g grasa
- Aguacate (100g): 160 cal, 2g proteína, 9g carbos, 15g grasa
- Huevo (1 unidad ~50g): 70 cal, 6g proteína, 0.6g carbos, 5g grasa
- Pan integral (1 rebanada ~30g): 70 cal, 3g proteína, 12g carbos, 1g grasa
- Frijoles negros (100g): 132 cal, 8.9g proteína, 23.7g carbos, 0.5g grasa
- Plátano (1 mediano ~120g): 105 cal, 1.3g proteína, 27g carbos, 0.4g grasa`;

export class VisionService {
  private genAI: GoogleGenerativeAI;

  constructor() {
    this.genAI = new GoogleGenerativeAI(config.ai.geminiApiKey);
  }

  async analyzeMealImage(imagePath: string): Promise<VisionAnalysisResult> {
    try {
      const imageBuffer = await fs.readFile(imagePath);
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

      const geminiResult = await model.generateContent([prompt, imagePart]);
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
      
      let analysisResult: VisionAnalysisResult;
      try {
        analysisResult = JSON.parse(jsonStr);
      } catch (e) {
        logger.error('Error parseando JSON:', { text: responseText, error: e });
        // Intento de recuperación básica o re-lanzar
        throw new Error('Formato de respuesta inválido (JSON malformado)');
      }
      
      // Validar estructura básica
      if (!analysisResult.foods || !Array.isArray(analysisResult.foods)) {
        throw new Error('Respuesta del modelo incompleta (sin array de alimentos)');
      }

      return analysisResult;
    } catch (error) {
      logger.error('Error en análisis de visión:', error);
      throw new Error('Error al analizar la imagen de comida');
    }
  }

  async analyzeTextDescription(description: string): Promise<VisionAnalysisResult> {
    try {
      const TEXT_ANALYSIS_PROMPT = `${VISION_SYSTEM_PROMPT}

DESCRIPCIÓN DE COMIDA: "${description}"

Analiza esta descripción de comida y proporciona información nutricional siguiendo el formato especificado. Estima cantidades razonables basándote en porciones típicas.`;

      const model = this.genAI.getGenerativeModel({ model: 'gemini-3-flash-preview' });

      const geminiResult = await model.generateContent(TEXT_ANALYSIS_PROMPT);
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
      
      let analysisResult: VisionAnalysisResult;
      try {
        analysisResult = JSON.parse(jsonStr);
      } catch (e) {
        logger.error('Error parseando JSON:', { text: responseText, error: e });
        throw new Error('Formato de respuesta inválido (JSON malformado)');
      }
      
      // Validar estructura básica
      if (!analysisResult.foods || !Array.isArray(analysisResult.foods)) {
        throw new Error('Respuesta del modelo incompleta (sin array de alimentos)');
      }

      return analysisResult;
    } catch (error) {
      logger.error('Error en análisis de texto:', error);
      throw new Error('Error al analizar la descripción de comida');
    }
  }

  async chatNutrition(message: string, conversationHistory?: any[]): Promise<{ message: string; shouldRegisterMeal: boolean; mealData?: any }> {
    try {
      const CHAT_SYSTEM_PROMPT = `Eres un asistente nutricional amigable y experto. 

Tus responsabilidades:
1. Responder preguntas sobre nutrición, dietas y salud
2. Ofrecer consejos personalizados sobre alimentación
3. Ayudar a interpretar información nutricional
4. Detectar cuando el usuario menciona haber comido algo

Cuando el usuario mencione que comió algo:
- Responde de forma natural y amigable
- Analiza la descripción nutricional
- Devuelve shouldRegisterMeal: true y los datos de la comida

Formato de respuesta (JSON):
{
  "message": "tu respuesta amigable al usuario",
  "shouldRegisterMeal": false,
  "mealData": null
}

O si detectas una comida:
{
  "message": "¡Qué delicioso! He registrado tu comida.",
  "shouldRegisterMeal": true,
  "mealData": {
    "foods": [
      {
        "name": "nombre del alimento",
        "amount": 100,
        "unit": "g",
        "nutrition": { "calories": 200, "protein": 10, "carbs": 20, "fat": 5, "fiber": 2 },
        "category": "protein|carb|vegetable|fruit|dairy|fat|mixed"
      }
    ],
    "totalNutrition": { "calories": 200, "protein": 10, "carbs": 20, "fat": 5, "fiber": 2 },
    "mealType": "breakfast|lunch|dinner|snack"
  }
}`;

      const model = this.genAI.getGenerativeModel({ model: 'gemini-3-flash-preview' });

      // Construir historial de conversación
      let conversationContext = '';
      if (conversationHistory && conversationHistory.length > 0) {
        conversationContext = '\n\nHistorial de conversación:\n';
        conversationHistory.forEach((msg: any) => {
          conversationContext += `${msg.role === 'user' ? 'Usuario' : 'Asistente'}: ${msg.content}\n`;
        });
      }

      const fullPrompt = `${CHAT_SYSTEM_PROMPT}${conversationContext}\n\nMensaje del usuario: "${message}"\n\nResponde en formato JSON.`;

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
          shouldRegisterMeal: false
        };
      }

      return {
        message: chatResult.message || responseText,
        shouldRegisterMeal: chatResult.shouldRegisterMeal || false,
        mealData: chatResult.mealData || undefined
      };
    } catch (error) {
      logger.error('Error en chat de nutrición:', error);
      throw new Error('Error al procesar el mensaje');
    }
  }
}
