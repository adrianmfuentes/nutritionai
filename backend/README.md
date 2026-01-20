# Nutrition AI Backend

Backend para aplicación de seguimiento nutricional con IA que analiza imágenes de comidas usando **LLaMA 3.2 90B Vision via Groq**.

## 🚀 Características

- 🤖 Análisis de imágenes de comidas con IA (**LLaMA 3.2 90B Vision via Groq - 100% GRATIS**)
- 📊 Seguimiento nutricional completo (calorías, proteínas, carbos, grasas)
- 👤 Autenticación JWT
- 🗄️ PostgreSQL para almacenamiento
- 🐳 Completamente containerizado con Docker
- 🔒 Seguro (SSL/TLS, rate limiting, helmet)
- 📈 APIs RESTful bien estructuradas

## 📋 Requisitos Previos

- Docker y Docker Compose
- Node.js 20+ (para desarrollo local)
- API Key de Groq (100% GRATIS - https://console.groq.com/)

## 🛠️ Instalación y Configuración

### 1. Clonar Repositorio

```bash
git clone <tu-repo>
cd nutrition-app/backend
```

### 2. Configurar Variables de Entorno

```bash
cp .env.example .env
nano .env
```

Edita `.env` con tus valores:

```env
# Database
DB_PASSWORD=tu_password_seguro_aqui

# Authentication
JWT_SECRET=tu_jwt_secret_super_seguro

# AI Service
GROQ_API_KEY=gsk-tu-api-key-aqui
```

### 3. Desplegar con Docker

```bash
# Construir y levantar todos los servicios
docker-compose up -d --build

# Ver logs
docker-compose logs -f api

# Verificar salud
curl http://localhost/health
```

## 📁 Estructura del Proyecto

```
backend/
├── docker-compose.yml       # Configuración Docker
├── Dockerfile              # Imagen de la aplicación
├── init.sql               # Schema de base de datos
├── nginx/
│   └── nginx.conf         # Configuración reverse proxy
├── src/
│   ├── config/           # Configuración (DB, env)
│   ├── middleware/       # Auth, upload, errores
│   ├── routes/          # Rutas API
│   ├── controllers/     # Lógica de negocio
│   ├── services/        # Servicios (Vision AI, Storage)
│   ├── models/          # Modelos de datos
│   ├── types/           # TypeScript types
│   └── utils/           # Utilidades (JWT, logger)
```

## 🔌 API Endpoints

### Autenticación

```
POST /v1/auth/register
POST /v1/auth/login
GET  /v1/profile
```

### Comidas

```
POST   /v1/meals/analyze       # Analizar imagen de comida
GET    /v1/meals               # Listar comidas
GET    /v1/meals/:id          # Obtener comida específica
PATCH  /v1/meals/:id          # Actualizar notas
DELETE /v1/meals/:id          # Eliminar comida
```

### Nutrición

```
GET /v1/nutrition/daily        # Resumen diario
GET /v1/nutrition/weekly       # Resumen semanal
PUT /v1/nutrition/goals        # Actualizar objetivos
```

## 📝 Ejemplos de Uso

### Registrar Usuario

```bash
curl -X POST http://localhost/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "Usuario Test"
  }'
```

### Analizar Comida

```bash
curl -X POST http://localhost/v1/meals/analyze \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "image=@meal.jpg" \
  -F "mealType=lunch"
```

### Obtener Resumen Diario

```bash
curl http://localhost/v1/nutrition/daily?date=2024-01-15 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🐳 Comandos Docker Útiles

```bash
# Ver estado de servicios
docker-compose ps

# Logs de base de datos
docker-compose logs -f postgres

# Reiniciar API
docker-compose restart api

# Detener todos los servicios
docker-compose down

# Recrear contenedor API
docker-compose up -d --force-recreate api

# Ejecutar comando en contenedor
docker-compose exec api npm run migrate
```

## 🔧 Desarrollo Local

```bash
# Instalar dependencias
npm install

# Ejecutar en modo desarrollo
npm run dev

# Build
npm run build

# Ejecutar producción
npm start
```

## 🔒 Seguridad

- Contraseñas hasheadas con bcrypt (12 rounds)
- JWT para autenticación
- Rate limiting en todos los endpoints
- Helmet para headers de seguridad
- CORS configurado
- Validación de inputs con Zod
- SQL injection protection (prepared statements)
- File upload validation

## 📊 Base de Datos

El schema incluye:

- `users` - Usuarios del sistema
- `nutrition_goals` - Objetivos nutricionales
- `meals` - Comidas registradas
- `detected_foods` - Alimentos detectados por IA

Ver `init.sql` para el schema completo.

## 🚨 Troubleshooting

### Error de conexión a base de datos

```bash
# Verificar que PostgreSQL está corriendo
docker-compose ps postgres

# Ver logs
docker-compose logs postgres
```

### Error de API Key

Verifica que `GROQ_API_KEY` esté configurada correctamente en `.env`.

### Problemas con uploads

```bash
# Verificar permisos del directorio
ls -la uploads/

# Crear directorio si no existe
mkdir -p uploads/temp
```

## 📄 Licencia

MIT

## 👥 Autor

Tu Nombre
