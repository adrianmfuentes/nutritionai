# Nutrition AI - Project Context

Contexto, reglas y esquemas de datos que todo agente de IA debe seguir al trabajar con este código.

## Vision

App de seguimiento nutricional: el usuario saca una foto de su plato, la API la manda a LLaMA 3.2 90B Vision (Groq), y devuelve los macros. Arquitectura: Android nativo (Kotlin/Compose) + API REST (Node/TypeScript).

---

## Reglas globales

- **Código**: inglés (variables, funciones, comentarios).
- **UI**: español (textos visibles al usuario).
- **Secretos**: todo en `.env`, nunca hardcodeado.

## Android

- Kotlin + Jetpack Compose (Material Design 3).
- Arquitectura MVVM.
- Retrofit + OkHttp para red. Coil para imágenes. CameraX para captura.
- Comprimir imágenes antes de subir (max 1MB).
- Si la API devuelve `is_food: false`, mostrar mensaje claro al usuario.
- Mostrar `detected_ingredients` como chips/etiquetas en los resultados.
- Mostrar `reasoning` en un tooltip o desplegable.

## Backend

- Node.js 20 + TypeScript (strict mode) + Express.
- Groq SDK para IA. Zod para validación.
- Nunca confiar en lo que devuelve el LLM sin validar contra el schema de abajo.
- Si el JSON es inválido, reintentar 1 vez.
- Verificar `if (!response.is_food)` antes de guardar en DB.
- Calcular totales sumando los `items`, no usar el total que da la IA.
- Guardar el JSON crudo del análisis en `meals.ai_metadata` (JSONB).

---

## Schema de respuesta de la IA

El backend debe forzar este schema en el system prompt. Android debe poder renderizarlo.

```json
{
  "is_food": true,
  "error": null,
  "reasoning": "Explicacion de como estimo porciones y macros",
  "foods": [
    {
      "name": "Nombre en espanol (ej: Arroz con Pollo)",
      "detected_ingredients": ["Arroz", "Pollo", "Guisantes"],
      "portion_display": "1 taza (200g)",
      "portion_grams": 200,
      "nutrition": {
        "calories": 250,
        "protein": 15,
        "carbs": 40,
        "fat": 5,
        "fiber": 2
      },
      "category": "mixed",
      "confidence": 0.95
    }
  ],
  "meal_analysis": {
    "health_score": 85,
    "health_feedback": "Buen balance de proteinas y carbos.",
    "dominant_macro": "carbs"
  }
}
```

Categorías válidas: `protein`, `carb`, `vegetable`, `fruit`, `dairy`, `fat`, `mixed`.
