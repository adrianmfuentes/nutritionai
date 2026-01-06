# Testing con cURL

Ejemplos de cómo probar todos los endpoints del backend usando cURL.

## Variables de Entorno

```bash
# Configurar estas variables
export API_URL="http://localhost:3000/v1"
export TOKEN=""  # Se llenará después del login
```

## 1. Health Check

```bash
curl -X GET http://localhost:3000/health
```

**Respuesta esperada:**

```json
{
  "status": "ok",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## 2. Registro de Usuario

```bash
curl -X POST $API_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123456",
    "name": "Usuario Test"
  }'
```

**Respuesta esperada:**

```json
{
  "message": "Usuario registrado exitosamente",
  "user": {
    "id": "uuid-here",
    "email": "test@example.com",
    "name": "Usuario Test"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Guardar token:**

```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 3. Login

```bash
curl -X POST $API_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123456"
  }'
```

## 4. Obtener Perfil

```bash
curl -X GET $API_URL/profile \
  -H "Authorization: Bearer $TOKEN"
```

## 5. Analizar Comida

```bash
# Crear una imagen de prueba o usar una existente
curl -X POST $API_URL/meals/analyze \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@/path/to/meal-image.jpg" \
  -F "mealType=lunch"
```

**Con timestamp personalizado:**

```bash
curl -X POST $API_URL/meals/analyze \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@meal.jpg" \
  -F "mealType=breakfast" \
  -F "timestamp=2024-01-15T08:30:00Z"
```

**Respuesta esperada:**

```json
{
  "mealId": "uuid-here",
  "detectedFoods": [
    {
      "name": "Huevos revueltos",
      "confidence": 0.92,
      "portion": {
        "amount": 100,
        "unit": "g"
      },
      "nutrition": {
        "calories": 140,
        "protein": 12,
        "carbs": 1,
        "fat": 10
      }
    }
  ],
  "totalNutrition": {
    "calories": 350,
    "protein": 25,
    "carbs": 30,
    "fat": 15
  },
  "imageUrl": "/uploads/user-id/meal_123.jpg",
  "timestamp": "2024-01-15T08:30:00Z",
  "notes": "Desayuno equilibrado con buena fuente de proteína"
}
```

## 6. Listar Comidas

**Todas las comidas:**

```bash
curl -X GET "$API_URL/meals?limit=10&offset=0" \
  -H "Authorization: Bearer $TOKEN"
```

**Comidas de una fecha específica:**

```bash
curl -X GET "$API_URL/meals?date=2024-01-15" \
  -H "Authorization: Bearer $TOKEN"
```

## 7. Obtener Comida Específica

```bash
# Reemplazar MEAL_ID con el ID real
curl -X GET $API_URL/meals/MEAL_ID \
  -H "Authorization: Bearer $TOKEN"
```

## 8. Actualizar Notas de Comida

```bash
curl -X PATCH $API_URL/meals/MEAL_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Comida post-entrenamiento"
  }'
```

## 9. Eliminar Comida

```bash
curl -X DELETE $API_URL/meals/MEAL_ID \
  -H "Authorization: Bearer $TOKEN"
```

## 10. Resumen Diario

**Día actual:**

```bash
curl -X GET $API_URL/nutrition/daily \
  -H "Authorization: Bearer $TOKEN"
```

**Día específico:**

```bash
curl -X GET "$API_URL/nutrition/daily?date=2024-01-15" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**

```json
{
  "date": "2024-01-15",
  "totals": {
    "calories": 1850,
    "protein": 120,
    "carbs": 180,
    "fat": 60,
    "fiber": 25
  },
  "goals": {
    "calories": 2000,
    "protein": 150,
    "carbs": 200,
    "fat": 65
  },
  "progress": {
    "caloriesPercent": 92,
    "proteinPercent": 80,
    "carbsPercent": 90,
    "fatPercent": 92
  },
  "meals": [...]
}
```

## 11. Resumen Semanal

**Últimos 7 días:**

```bash
curl -X GET $API_URL/nutrition/weekly \
  -H "Authorization: Bearer $TOKEN"
```

**Desde fecha específica:**

```bash
curl -X GET "$API_URL/nutrition/weekly?startDate=2024-01-08" \
  -H "Authorization: Bearer $TOKEN"
```

## 12. Actualizar Objetivos Nutricionales

```bash
curl -X PUT $API_URL/nutrition/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dailyCalories": 2200,
    "proteinGrams": 160,
    "carbsGrams": 220,
    "fatGrams": 70
  }'
```

## Scripts de Testing Completo

### test-full-flow.sh

```bash
#!/bin/bash

API_URL="http://localhost:3000/v1"
EMAIL="test-$(date +%s)@example.com"
PASSWORD="password123456"

echo "=== Testing Full API Flow ==="
echo ""

# 1. Register
echo "1. Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST $API_URL/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\",
    \"name\": \"Test User\"
  }")

echo $REGISTER_RESPONSE | jq .

TOKEN=$(echo $REGISTER_RESPONSE | jq -r .token)

if [ "$TOKEN" == "null" ]; then
  echo "❌ Registration failed"
  exit 1
fi

echo "✅ User registered"
echo "Token: $TOKEN"
echo ""

# 2. Get Profile
echo "2. Getting profile..."
curl -s -X GET $API_URL/profile \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 3. Analyze Meal (requiere imagen)
if [ -f "test-meal.jpg" ]; then
  echo "3. Analyzing meal..."
  MEAL_RESPONSE=$(curl -s -X POST $API_URL/meals/analyze \
    -H "Authorization: Bearer $TOKEN" \
    -F "image=@test-meal.jpg" \
    -F "mealType=lunch")

  echo $MEAL_RESPONSE | jq .
  MEAL_ID=$(echo $MEAL_RESPONSE | jq -r .mealId)
  echo "Meal ID: $MEAL_ID"
  echo ""
fi

# 4. Get Daily Summary
echo "4. Getting daily summary..."
curl -s -X GET $API_URL/nutrition/daily \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 5. Update Goals
echo "5. Updating nutrition goals..."
curl -s -X PUT $API_URL/nutrition/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dailyCalories": 2200,
    "proteinGrams": 160,
    "carbsGrams": 220,
    "fatGrams": 70
  }' | jq .
echo ""

echo "=== Testing Complete ==="
```

**Ejecutar:**

```bash
chmod +x test-full-flow.sh
./test-full-flow.sh
```

## Casos de Error

### 1. Sin autenticación

```bash
curl -X GET $API_URL/profile
```

**Respuesta:**

```json
{
  "error": "Token de autenticación requerido"
}
```

### 2. Token inválido

```bash
curl -X GET $API_URL/profile \
  -H "Authorization: Bearer invalid-token"
```

**Respuesta:**

```json
{
  "error": "Token inválido o expirado"
}
```

### 3. Email duplicado

```bash
# Registrar mismo email dos veces
curl -X POST $API_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test"
  }'
```

**Respuesta:**

```json
{
  "error": "El email ya está registrado"
}
```

### 4. Archivo no permitido

```bash
curl -X POST $API_URL/meals/analyze \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@document.pdf"
```

**Respuesta:**

```json
{
  "error": "Tipo de archivo no permitido. Solo se aceptan imágenes (JPEG, PNG, WebP)"
}
```

### 5. Validación fallida

```bash
curl -X POST $API_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "123",
    "name": "T"
  }'
```

**Respuesta:**

```json
{
  "error": "Validación fallida",
  "details": [
    {
      "path": ["email"],
      "message": "Email inválido"
    },
    {
      "path": ["password"],
      "message": "La contraseña debe tener al menos 8 caracteres"
    }
  ]
}
```

## Testing con Postman

### Importar Collection

1. Crear nueva Collection en Postman
2. Configurar variables:

   - `base_url`: `http://localhost:3000/v1`
   - `token`: (se llenará automáticamente)

3. Añadir requests según los ejemplos anteriores

### Pre-request Script para Auth

En la pestaña "Pre-request Script" de requests protegidos:

```javascript
// Auto-refresh token si ha expirado
const token = pm.environment.get("token");
if (!token) {
  pm.sendRequest(
    {
      url: pm.environment.get("base_url") + "/auth/login",
      method: "POST",
      header: {
        "Content-Type": "application/json",
      },
      body: {
        mode: "raw",
        raw: JSON.stringify({
          email: pm.environment.get("test_email"),
          password: pm.environment.get("test_password"),
        }),
      },
    },
    function (err, res) {
      if (!err) {
        pm.environment.set("token", res.json().token);
      }
    }
  );
}
```

## Notas

- Todos los endpoints usan JSON excepto el upload de imágenes (multipart/form-data)
- El token JWT expira en 7 días por defecto
- Rate limit: 100 requests por 15 minutos (general), 10 análisis por 15 minutos
- Máximo tamaño de archivo: 10MB
- Formatos de imagen aceptados: JPEG, PNG, WebP
