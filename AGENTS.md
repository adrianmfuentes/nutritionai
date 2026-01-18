# 🤖 Nutrition AI - Project Context & Agents Configuration

Este archivo define el contexto, roles y directrices para los Agentes de IA que interactúan con el código de **Nutrition AI**.

## 🌍 Visión del Proyecto
Una aplicación de seguimiento nutricional "End-to-End" que utiliza **LLaMA 3.2 90B Vision (vía Groq)** para analizar fotos de comida y devolver macros precisos. La arquitectura se divide en un cliente Android nativo y una API RESTful en Node.js/TypeScript.

---

## 🏗️ Tech Stack & Constraints

### Global
- **Idioma del Código**: Inglés (variables, funciones, comentarios).
- **Idioma de la UI**: Español (textos visibles para el usuario).
- **Gestión de Configuración**: Todo secreto/clave API debe ir en `.env`.

### 📱 Frontend (Android)
- **Lenguaje**: Kotlin.
- **UI Framework**: Jetpack Compose (Material Design 3).
- **Arquitectura**: MVVM (Model-View-ViewModel).
- **Networking**: Retrofit + OkHttp.
- **Imágenes**: Coil (carga), CameraX (captura).
- **Regla de Oro**: Manejar elegantemente los estados de carga y error. Si la API devuelve `is_food: false`, mostrar un mensaje amigable al usuario.

### 🔙 Backend (API)
- **Runtime**: Node.js 20 + TypeScript (Strict Mode).
- **Framework**: Express.js.
- **AI Provider**: Groq SDK (LLaMA 3.2 90B Vision).
- **Validación**: Zod.
- **Regla de Oro**: NUNCA confiar en la salida cruda del LLM. Validar siempre contra el esquema JSON definido abajo antes de guardar en DB.

---

## 👥 Agentes Especializados

### 1. `@Agent_Android_Dev`
**Rol**: Experto en Desarrollo Móvil Moderno.
**Contexto**: Carpeta `/frontend`.
* **Directrices**:
    * Al mostrar los resultados de la comida, busca el campo `detected_ingredients` para mostrar chips o etiquetas detalladas.
    * Muestra el campo `reasoning` (Razonamiento de la IA) en un desplegable o tooltip para dar transparencia al usuario.
    * Gestiona la compresión de imagen antes de subirla (máx 1MB) para no saturar la red.

### 2. `@Agent_Backend_Architect`
**Rol**: Ingeniero de Backend y AI Integration.
**Contexto**: Carpeta `/backend`.
* **Directrices**:
    * Implementar el **Vision System Prompt V2** (definido en Data Contracts).
    * En el servicio de IA, verificar `if (!response.is_food)` antes de guardar nada en la base de datos.
    * Si la IA falla al generar JSON válido, implementar un mecanismo de "retry" (máximo 1 reintento).
    * Calcular los totales nutricionales sumando los `items` individuales en el servidor, no confiar ciegamente en el total que da la IA.

### 3. `@Agent_Database_Admin`
**Rol**: Administrador de PostgreSQL.
* **Directrices**:
    * La tabla `meals` debe almacenar el JSON crudo del análisis en una columna `ai_metadata` (tipo JSONB) para futuros re-entrenamientos o depuración.

---

## 📜 Data Contracts & Schemas (Fuente de la Verdad)

Cualquier interacción entre el Backend y el servicio de IA (Groq) **DEBE** adherirse estrictamente a este esquema.

### Vision AI Response Schema (JSON)
El Agente de Backend debe forzar este esquema en el System Prompt, y el Agente de Android debe estar listo para renderizarlo.

```json
{
  "is_food": true, // Booleano crítico. Si es false, mostrar error.
  "error": null,   // String si is_food es false (ej: "No food detected")
  "reasoning": "Texto explicativo sobre cómo la IA calculó el tamaño",
  "foods": [
    {
      "name": "Nombre en Español (ej: Arroz con Pollo)",
      "detected_ingredients": ["Arroz", "Pollo", "Guisantes"],
      "portion_display": "ej: 1 taza (200g)",
      "portion_grams": 200,
      "nutrition": {
        "calories": 250,
        "protein": 15,
        "carbs": 40,
        "fat": 5,
        "fiber": 2
      },
      "category": "mixed", // protein, carb, vegetable, fruit, dairy, fat, mixed
      "confidence": 0.95
    }
  ],
  "meal_analysis": {
    "health_score": 85, // Escala 0-100
    "health_feedback": "Buen balance de proteínas y carbos.",
    "dominant_macro": "carbs"
  }
}
