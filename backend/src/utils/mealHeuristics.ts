// src/utils/mealHeuristics.ts

function normalize(text: string): string {
  return (text || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

const NON_FOOD_PHRASES = new Set([
  'hola',
  'buenas',
  'buenos dias',
  'buenas tardes',
  'buenas noches',
  'hey',
  'que tal',
  'como estas',
  'como estas?',
  'ok',
  'vale',
  'gracias',
  'muchas gracias',
  'adios',
]);

const STOPWORDS = new Set([
  'yo', 'me', 'mi', 'mis', 'tu', 'tus', 'el', 'la', 'los', 'las', 'un', 'una', 'unos', 'unas',
  'de', 'del', 'al', 'y', 'o', 'en', 'con', 'sin', 'para', 'por', 'a', 'que', 'como', 'muy',
  'hoy', 'ayer', 'manana', 'anoche', 'ahora',
  'comi', 'he', 'comido', 'ceno', 'cene', 'desayune', 'desayuno', 'almuerzo', 'merienda',
]);

export function isLikelyMealDescription(input: string): { ok: boolean; reason?: string } {
  const text = normalize(input);
  if (!text) return { ok: false, reason: 'La descripción está vacía.' };

  if (NON_FOOD_PHRASES.has(text)) {
    return { ok: false, reason: 'El mensaje no parece describir una comida.' };
  }

  // Si es extremadamente corto, suele ser ruido (ej. "hola")
  if (text.length < 4) {
    return { ok: false, reason: 'La descripción es demasiado corta para identificar alimentos.' };
  }

  const tokens = text.split(' ').filter(Boolean);
  const contentTokens = tokens.filter((t) => !STOPWORDS.has(t) && t.length >= 3);

  // Si tras quitar stopwords no queda nada, probablemente no es comida.
  if (contentTokens.length === 0) {
    return { ok: false, reason: 'No se detectaron nombres de alimentos en el texto.' };
  }

  // Si el usuario da al menos 3 tokens con contenido, lo aceptamos aunque no estén en un diccionario.
  if (contentTokens.length >= 3) {
    return { ok: true };
  }

  // Si hay números ("2 huevos") suele ser descripción de comida.
  if (/[0-9]/.test(text)) {
    return { ok: true };
  }

  // Si solo hay 1-2 tokens de contenido, exigimos un mínimo de longitud total
  if (text.length >= 12) {
    return { ok: true };
  }

  return { ok: false, reason: 'La descripción no parece suficiente para identificar una comida.' };
}

export function isClearlyNonFoodName(name: string): boolean {
  const text = normalize(name);
  if (!text) return true;
  if (NON_FOOD_PHRASES.has(text)) return true;

  // Nombres genéricos que suelen venir de respuestas no estructuradas
  if (text === 'mensaje' || text === 'texto' || text === 'respuesta') return true;

  // Si es muy corto y sin números, sospechoso
  if (text.length < 3 && !/[0-9]/.test(text)) return true;

  return false;
}
