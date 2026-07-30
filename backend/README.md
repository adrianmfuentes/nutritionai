# Nutrition AI - Backend

API REST en Node.js + TypeScript que recibe fotos de comida, las pasa por LLaMA 3.2 90B Vision (Groq) y devuelve los macros.

## Requisitos

- Docker + Docker Compose
- Node.js 20+ (si corrés sin Docker)
- API key de Groq (https://console.groq.com, es gratis)

## Setup

```bash
cd backend
cp .env.example .env   # editá con tus valores
```

Variables necesarias en `.env`:

```env
DB_PASSWORD=...
JWT_SECRET=...
GROQ_API_KEY=gsk-...
```

```bash
docker-compose up -d --build
curl http://localhost/health
```

## Endpoints

```
POST   /v1/auth/register
POST   /v1/auth/login
GET    /v1/profile

POST   /v1/meals/analyze       # multipart: image + mealType
GET    /v1/meals
GET    /v1/meals/:id
PATCH  /v1/meals/:id
DELETE /v1/meals/:id

GET    /v1/nutrition/daily?date=...
GET    /v1/nutrition/weekly?startDate=...
PUT    /v1/nutrition/goals
```

## Ejemplos

```bash
# registrar
curl -X POST http://localhost/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456","name":"Test"}'

# analizar foto
curl -X POST http://localhost/v1/meals/analyze \
  -H "Authorization: Bearer TOKEN" \
  -F "image=@plato.jpg" \
  -F "mealType=lunch"

# resumen del dia
curl http://localhost/v1/nutrition/daily?date=2024-01-15 \
  -H "Authorization: Bearer TOKEN"
```

## Estructura

```
backend/
├── docker-compose.yml
├── Dockerfile
├── init.sql
├── nginx/nginx.conf
└── src/
    ├── config/        # DB, env
    ├── middleware/    # auth, upload, rate limit, errores
    ├── routes/        # definicion de endpoints
    ├── controllers/   # logica por ruta
    ├── services/      # Groq Vision, storage
    ├── models/        # consultas SQL
    ├── types/         # tipos TypeScript
    └── utils/         # JWT, logger, helpers
```

## Seguridad

bcrypt (12 rounds), JWT, rate limiting por IP, Helmet, CORS, Zod para validar inputs, prepared statements, validacion de archivos subidos.

## Base de datos

Tablas: `users`, `nutrition_goals`, `meals`, `detected_foods`. El schema completo está en `init.sql`.

## Docker

```bash
docker-compose ps                  # estado
docker-compose logs -f api         # logs del backend
docker-compose restart api         # reiniciar
docker-compose down                # frenar todo
docker-compose up -d --force-recreate api   # recrear solo api
docker-compose exec api npm run migrate
```

## Desarrollo sin Docker

```bash
npm install
npm run dev       # modo dev
npm run build     # compilar
npm start         # produccion
```

## Problemas comunes

**No conecta a DB**: `docker-compose ps postgres`, revisar que esté `Up`.

**Error de API key**: verificar `GROQ_API_KEY` en `.env`.

**Falla el upload**: revisar que exista `uploads/temp` y tenga permisos de escritura.

## Licencia

MIT
