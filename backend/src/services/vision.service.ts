// src/services/vision.service.ts
import Groq from 'groq-sdk';
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
    "estimatedMealType": "lunch",
    "portionSize": "medium",
    "healthScore": 7.5
  },
  "notes": "Observación breve sobre la composición de la comida"
}

REGLAS:
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
  private client: Groq;

  constructor() {
    this.client = new Groq({
      apiKey: config.ai.groqApiKey,
    });
  }

  async analyzeMealImage(imagePath: string): Promise<VisionAnalysisResult> {
    try {
      const imageBuffer = await fs.readFile(imagePath);
      const base64Image = imageBuffer.toString('base64');
      
      // Determinar el tipo MIME
      const ext = path.extname(imagePath).toLowerCase();
      const mediaType = ext === '.png' ? 'image/png' : 'image/jpeg';

      const completion = await this.client.chat.completions.create({
        model: 'llama-3.2-90b-vision-preview',
        messages: [
          {
            role: 'system',
            content: VISION_SYSTEM_PROMPT,
          },
          {
            role: 'user',
            content: [
              {
                type: 'text',
                text: 'Analiza esta imagen de comida y proporciona información nutricional siguiendo el formato especificado.',
              },
              {
                type: 'image_url',
                image_url: {
                  url: `data:${mediaType};base64,${base64Image}`,
                },
              },
            ],
          },
        ],
        temperature: 0.3,
        max_tokens: 2000,
        top_p: 1,
      });

      const responseText = completion.choices[0]?.message?.content;
      if (!responseText) {
        throw new Error('No se recibió respuesta del modelo');
      }

      // Parsear JSON
      const jsonMatch = responseText.match(/\{[\s\S]*\}/);
      if (!jsonMatch) {
        logger.error('No se encontró JSON en la respuesta:', responseText);
        throw new Error('Formato de respuesta inválido');
      }

      const result: VisionAnalysisResult = JSON.parse(jsonMatch[0]);
      
      // Validar estructura básica
      if (!result.foods || !Array.isArray(result.foods)) {
        throw new Error('Respuesta sin alimentos detectados');
      }

      return result;
    } catch (error) {
      logger.error('Error en análisis de visión:', error);
      throw new Error('Error al analizar la imagen de comida');
    }
  }
}
