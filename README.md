# 🍽️ Nutrition AI

Aplicación completa de seguimiento nutricional que utiliza Inteligencia Artificial para analizar imágenes de comidas y proporcionar información nutricional detallada.

## 📱 Arquitectura

- **Frontend**: Android nativo con Kotlin (Jetpack Compose)
- **Backend**: Node.js + TypeScript + Express
- **Base de Datos**: PostgreSQL
- **IA**: Groq (LLaMA 3.2 90B Vision)
- **Infraestructura**: Docker + Docker Compose
- **Servidor**: Oracle

## ✨ Características Principales

### Android

- 📸 Captura de fotos de comidas
- 🤖 Análisis automático de alimentos con IA
- 📊 Dashboard nutricional con progreso diario
- 🔒 Autenticación segura con JWT
- 💾 Almacenamiento local con Room
- 🎨 UI moderna con Material Design 3

### Backend API

- 🧠 Análisis de imágenes con IA
- 🍎 Detección automática de alimentos
- 📏 Estimación de porciones
- 🔢 Cálculo de macronutrientes
- 👤 Gestión de usuarios y perfiles
- 🎯 Sistema de objetivos personalizables
- 🔐 Seguridad completa (JWT, bcrypt, rate limiting)
- 📦 Completamente containerizado

## 🚀 Inicio Rápido

### Requisitos Previos

- Docker y Docker Compose
- Android Studio (para android)
- API Key de Groq (gratis en https://console.groq.com/)

### 📱 Opción 1: Pruebas Locales (Recomendado)

**Para probar en tu móvil antes de subir a producción:**

```bash
# 1. Obtén tu API key gratuita de Groq
# Visita: https://console.groq.com/

# 2. Obtén tu IP local
# Windows:
get-local-ip.bat

# Linux/Mac:
chmod +x get-local-ip.sh
./get-local-ip.sh

# 3. Configura el backend
cd backend
nano .env  # Añade tu GROQ_API_KEY

# 4. Levanta los servicios
docker-compose up -d --build

# 5. Configura la app Android con tu IP local
# Edita: android/app/src/main/.../ApiConfig.kt
# Cambia BASE_URL a: http://TU_IP:3000/v1/

# 6. Compila e instala en tu móvil
cd ../android
./gradlew installDebug
```

📖 **Guía completa:** [LOCAL_TESTING.md](backend/LOCAL_TESTING.md)

---

### 🌐 Opción 2: Configuración en Servidor (Producción)

```bash
cd backend

# Copiar variables de entorno
cp .env.example .env

# Editar .env con tus credenciales
nano .env

# Levantar servicios
docker-compose up -d --build

# Verificar
curl http://localhost:3000/health
```

### Configuración Android

```bash
cd android

# Abrir en Android Studio
# Configurar API URL en ApiConfig.kt
# Ejecutar en emulador o dispositivo
```

## 📁 Estructura del Proyecto

```
nutrition-app/
├── android/              # Aplicación Android
│   ├── app/
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/    # Código Kotlin
│   │       │   └── res/     # Recursos UI
│   │       └── test/
│   └── build.gradle.kts
│
└── backend/              # API Node.js
    ├── src/
    │   ├── config/       # Configuración
    │   ├── controllers/  # Lógica de negocio
    │   ├── middleware/   # Auth, upload, errores
    │   ├── models/       # Modelos de datos
    │   ├── routes/       # Rutas API
    │   ├── services/     # Vision AI, Storage
    │   └── utils/        # Utilidades
    ├── nginx/           # Reverse proxy
    ├── docker-compose.yml
    └── Dockerfile
```

## 🔌 API Endpoints

### Autenticación

```
POST   /v1/auth/register    - Registrar usuario
POST   /v1/auth/login       - Iniciar sesión
GET    /v1/profile          - Obtener perfil
```

### Comidas

```
POST   /v1/meals/analyze    - Analizar imagen
GET    /v1/meals            - Listar comidas
GET    /v1/meals/:id        - Obtener comida
DELETE /v1/meals/:id        - Eliminar comida
```

### Nutrición

```
GET    /v1/nutrition/daily  - Resumen diario
GET    /v1/nutrition/weekly - Resumen semanal
PUT    /v1/nutrition/goals  - Actualizar objetivos
```

## 🛠️ Tecnologías Utilizadas

### Android

- Kotlin
- Jetpack Compose
- CameraX
- Retrofit
- Room Database
- Coil (carga de imágenes)
- Material Design 3

### Backend

- Node.js 20
- TypeScript
- Express.js
- PostgreSQL
- **Groq API (LLaMA 3.2 90B Vision)** - 100% Gratis
- Docker
- Nginx
- JWT
- Multer (uploads)
- Sharp (procesamiento de imágenes)

## 🔐 Seguridad

- ✅ Autenticación JWT
- ✅ Contraseñas hasheadas con bcrypt (12 rounds)
- ✅ Rate limiting en todos los endpoints
- ✅ Helmet para headers de seguridad
- ✅ CORS configurado
- ✅ Validación de inputs con Zod
- ✅ SQL injection protection
- ✅ File upload validation
- ✅ SSL/TLS en producción

## 📊 Base de Datos

### Schema Principal

- **users** - Usuarios del sistema
- **nutrition_goals** - Objetivos nutricionales personalizados
- **meals** - Comidas registradas
- **detected_foods** - Alimentos detectados por IA

## 🌟 Características Destacadas

### Análisis Inteligente con IA

- Identificación automática de alimentos
- Estimación precisa de porciones
- Cálculo de macronutrientes
- Puntuación de salud de comidas
- Detección de categorías (proteína, carbos, etc.)

### Dashboard Nutricional

- Progreso diario en tiempo real
- Comparación con objetivos
- Gráficos interactivos
- Historial de comidas
- Tendencias semanales

### Experiencia de Usuario

- Captura rápida de fotos
- Análisis en segundos
- Interfaz intuitiva
- Modo oscuro/claro
- Notificaciones de progreso

## 🚢 Deployment

### Desarrollo Local

```bash
# Backend
cd backend
docker-compose up -d

# Android
cd android
./gradlew installDebug
```

### Producción (Oracle Ampere)

```bash
cd backend
chmod +x deploy.sh
./deploy.sh
```

## 🧪 Testing

```bash
# Backend
cd backend
npm test

# Test manual con cURL
./test-full-flow.sh

# Android
cd android
./gradlew test
```

## 📝 Configuración

### Configuración Android

```kotlin
// ApiConfig.kt
private const val BASE_URL = "https://tu-api.com/v1/"
```

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/amazing-feature`)
3. Commit cambios (`git commit -m 'Add amazing feature'`)
4. Push a la rama (`git push origin feature/amazing-feature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.
