# API Endpoints Documentation

## Base URL

```
http://localhost/v1
```

## Authentication

Todas las rutas protegidas requieren un header de autorización:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## Auth Endpoints

### Register User

**POST** `/auth/register`

**Body:**

```json
{
  "email": "user@example.com",
  "password": "securepassword123",
  "name": "Juan Pérez"
}
```

**Response:** `201 Created`

```json
{
  "message": "Usuario registrado exitosamente",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "Juan Pérez"
  },
  "token": "eyJhbGc..."
}
```

### Login

**POST** `/auth/login`

**Body:**

```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Response:** `200 OK`

```json
{
  "message": "Inicio de sesión exitoso",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "Juan Pérez"
  },
  "token": "eyJhbGc..."
}
```

---

## Profile Endpoints

### Get Profile

**GET** `/profile`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "Juan Pérez",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

---

## Meals Endpoints

### Analyze Meal Image

**POST** `/meals/analyze`

**Headers:**

- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**Form Data:**

- `image` (file): Imagen de la comida (JPEG, PNG, WebP, max 10MB)
- `mealType` (optional): `breakfast`, `lunch`, `dinner`, `snack`
- `timestamp` (optional): ISO 8601 datetime

**Response:** `201 Created`

```json
{
  "mealId": "uuid",
  "detectedFoods": [
    {
      "name": "Pollo a la plancha",
      "confidence": 0.95,
      "portion": {
        "amount": 150,
        "unit": "g"
      },
      "nutrition": {
        "calories": 247,
        "protein": 46.5,
        "carbs": 0,
        "fat": 5.4
      }
    }
  ],
  "totalNutrition": {
    "calories": 520,
    "protein": 65,
    "carbs": 45,
    "fat": 12
  },
  "imageUrl": "/uploads/user-id/meal_123.jpg",
  "timestamp": "2024-01-01T12:00:00Z",
  "notes": "Comida equilibrada con buena fuente de proteína"
}
```

### Get Meals

**GET** `/meals`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `date` (optional): Filtrar por fecha (YYYY-MM-DD)
- `limit` (optional): Número de resultados (default: 20)
- `offset` (optional): Offset para paginación (default: 0)

**Response:** `200 OK`

```json
{
  "meals": [
    {
      "id": "uuid",
      "meal_type": "lunch",
      "total_calories": 520,
      "total_protein": 65,
      "total_carbs": 45,
      "total_fat": 12,
      "health_score": 8.5,
      "consumed_at": "2024-01-01T12:00:00Z",
      "image_url": "/uploads/...",
      "foods": [...]
    }
  ],
  "pagination": {
    "total": 45,
    "limit": 20,
    "offset": 0,
    "pages": 3
  }
}
```

### Get Meal by ID

**GET** `/meals/:mealId`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "meal": {
    "id": "uuid",
    "meal_type": "lunch",
    "total_calories": 520,
    "foods": [...]
  }
}
```

### Update Meal

**PATCH** `/meals/:mealId`

**Headers:** `Authorization: Bearer <token>`

**Body:**

```json
{
  "notes": "Comida post-entrenamiento"
}
```

**Response:** `200 OK`

### Delete Meal

**DELETE** `/meals/:mealId`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Comida eliminada exitosamente"
}
```

---

## Nutrition Endpoints

### Get Daily Summary

**GET** `/nutrition/daily`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `date` (optional): Fecha específica (YYYY-MM-DD, default: hoy)

**Response:** `200 OK`

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

### Get Weekly Summary

**GET** `/nutrition/weekly`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `startDate` (optional): Fecha de inicio (YYYY-MM-DD, default: 7 días atrás)

**Response:** `200 OK`

```json
{
  "days": [
    {
      "date": "2024-01-15",
      "calories": 1850,
      "protein": 120,
      "carbs": 180,
      "fat": 60,
      "mealCount": 3,
      "healthScore": 8.2
    }
  ],
  "averages": {
    "calories": 1920,
    "protein": 125,
    "carbs": 185,
    "fat": 62
  },
  "trend": "stable"
}
```

### Update Nutrition Goals

**PUT** `/nutrition/goals`

**Headers:** `Authorization: Bearer <token>`

**Body:**

```json
{
  "dailyCalories": 2200,
  "proteinGrams": 160,
  "carbsGrams": 220,
  "fatGrams": 70
}
```

**Response:** `200 OK`

```json
{
  "goals": {
    "id": "uuid",
    "daily_calories": 2200,
    "daily_protein": 160,
    "daily_carbs": 220,
    "daily_fat": 70
  },
  "message": "Objetivos nutricionales actualizados"
}
```

---

## Error Responses

### 400 Bad Request

```json
{
  "error": "Validación fallida",
  "details": [...]
}
```

### 401 Unauthorized

```json
{
  "error": "Token de autenticación requerido"
}
```

### 404 Not Found

```json
{
  "error": "Recurso no encontrado"
}
```

### 429 Too Many Requests

```json
{
  "error": "Demasiadas solicitudes. Por favor intenta más tarde."
}
```

### 500 Internal Server Error

```json
{
  "error": "Error interno del servidor"
}
```
