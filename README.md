# Nutrition AI

App de seguimiento nutricional que analiza fotos de comida con IA y te dice los macros al instante. Usa Google Gemini (vision) para identificar alimentos en fotos, y Groq (Llama 3.3) para el análisis por texto y el chat nutricional.

## Arquitectura

| Capa | Tecnología |
|------|-----------|
| **Frontend** | Android nativo - Kotlin + Jetpack Compose |
| **Backend** | Node.js 20 + TypeScript + Express |
| **Base de Datos** | PostgreSQL |
| **IA** | Google Gemini (fotos) + Groq API, Llama 3.3 (texto y chat) - gratis |
| **Infra** | Docker + Docker Compose, Oracle Ampere |

## Qué hace

- Sacás una foto de tu plato, la app la manda al backend, la IA la analiza y te devuelve calorías, proteínas, carbs, grasas, fibra y un puntaje de salud.
- Dashboard con progreso diario/semanal y comparación contra tus objetivos.
- Autenticación con JWT, almacenamiento local con Room.

## Para probarlo (local, en tu móvil)

Necesitás:
- Docker + Docker Compose
- Android Studio
- API Key de Google Gemini (sacala gratis en https://aistudio.google.com/app/apikey) - para análisis de fotos
- API Key de Groq (sacala gratis en https://console.groq.com) - para análisis por texto y el chat

```bash
# 1. Conseguí tu IP local
# Windows:
get-local-ip.bat
# Linux/Mac:
chmod +x get-local-ip.sh && ./get-local-ip.sh

# 2. Configurá el backend
cd backend
# Editá .env y poné tu GEMINI_API_KEY y GROQ_API_KEY

# 3. Levantá los servicios
docker-compose up -d --build

# 4. En android/app/.../ApiConfig.kt, poné la IP que sacaste:
# BASE_URL = "http://TU_IP:3000/v1/"

# 5. Compilá e instalá en el celu
cd ../android
./gradlew installDebug
```

Guía más detallada en [LOCAL_TESTING.md](backend/LOCAL_TESTING.md).

## Para deployar en producción

```bash
cd backend
cp .env.example .env   # editá con tus credenciales
docker-compose up -d --build
curl http://localhost:3000/health   # verificá que responda
```

## API

```
POST   /v1/auth/register
POST   /v1/auth/login
GET    /v1/profile

POST   /v1/meals/analyze      # mandar imagen, recibir análisis
GET    /v1/meals
GET    /v1/meals/:id
DELETE /v1/meals/:id

GET    /v1/nutrition/daily
GET    /v1/nutrition/weekly
PUT    /v1/nutrition/goals
```

## Estructura

```
nutritionai/
├── android/                 # App Android (Kotlin + Compose)
│   └── app/src/
│       ├── main/java/       # ViewModels, repos, networking
│       └── main/res/        # Temas, strings, drawables
│
└── backend/                 # API (Node + Express + TS)
    ├── src/
    │   ├── config/          # DB, env, constantes
    │   ├── controllers/     # Lógica de cada endpoint
    │   ├── middleware/       # Auth, rate limit, uploads, errores
    │   ├── models/          # Esquemas DB (raw SQL)
    │   ├── routes/          # Definición de rutas
    │   ├── services/        # Gemini/Groq (IA), storage de imágenes
    │   └── utils/           # Helpers
    ├── nginx/               # Config reverse proxy
    ├── docker-compose.yml
    └── Dockerfile
```

## Stack técnico

**Android:** Kotlin, Jetpack Compose, CameraX, Retrofit, Room, Coil, Material Design 3

**Backend:** Node.js 20, TypeScript, Express, PostgreSQL, Google Generative AI SDK, Groq API, Docker, Nginx, JWT, bcrypt, Multer, Sharp, Zod

## Seguridad

JWT con refresh tokens, bcrypt (12 rounds), rate limiting por IP, Helmet, CORS restrictivo, validación de inputs con Zod, sanitización de uploads, SSL/TLS en prod.

## Base de datos

- `users` — datos de cuenta y auth
- `nutrition_goals` — objetivos de macros por usuario
- `meals` — cada comida registrada + metadata cruda de la IA (JSONB)
- `detected_foods` — alimentos individuales detectados

## Tests

```bash
# Backend
cd backend && npm test
./test-full-flow.sh          # test manual con curl

# Android
cd android && ./gradlew test
```

## Licencia

MIT — [LICENSE](LICENSE)
