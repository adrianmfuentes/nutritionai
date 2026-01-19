# Nutrition AI - Android

Aplicación Android nativa para análisis nutricional con IA, construida con Kotlin y Jetpack Compose.

## 🏗️ Arquitectura

La aplicación sigue la arquitectura MVVM (Model-View-ViewModel) con las siguientes capas:

- **UI Layer**: Jetpack Compose con Material Design 3
- **ViewModel Layer**: ViewModels con StateFlow para manejo de estado
- **Repository Layer**: Patrón Repository para abstracción de datos
- **Data Layer**:
  - Remote: Retrofit para comunicación con API REST
  - Local: Room Database para cache offline

## 🛠️ Stack Tecnológico

### Core

- **Kotlin**: Lenguaje de programación
- **Jetpack Compose**: UI moderna y declarativa
- **Material Design 3**: Sistema de diseño

### Arquitectura & DI

- **Hilt**: Inyección de dependencias
- **Coroutines & Flow**: Programación asíncrona
- **ViewModel & LiveData**: Manejo de estado

### Persistencia

- **Room**: Base de datos local SQLite
- **DataStore**: Almacenamiento de preferencias

### Red

- **Retrofit**: Cliente HTTP
- **OkHttp**: Interceptores y logging
- **Gson**: Serialización JSON

### Media

- **CameraX**: Captura de imágenes
- **Coil**: Carga de imágenes

### Navegación

- **Navigation Compose**: Navegación entre pantallas

### Permisos

- **Accompanist Permissions**: Manejo de permisos

## 📱 Características Implementadas

### ✅ Dashboard (Pantalla Principal)

- Visualización de calorías y macronutrientes del día
- Progreso hacia objetivos nutricionales
- Lista de comidas del día
- Navegación entre días
- Actualización en tiempo real

### ✅ Cámara

- Captura de fotos de comidas
- Análisis con IA en tiempo real
- Detección automática de alimentos
- Cálculo de macronutrientes
- Estados de carga y error

### ✅ Historial

- Lista de todas las comidas registradas
- Swipe para eliminar
- Información detallada por comida

### 🔄 Sistema de Base de Datos Local

- Cache de comidas para acceso offline
- Sincronización con backend
- Relaciones entre entidades (Meals y Foods)

## 🚀 Configuración del Proyecto

### Requisitos Previos

- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 26+ (Oreo o superior)
- Gradle 8.13.2+

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd nutrition-app/android
```

### 2. Configurar la URL de la API

Edita `app/build.gradle.kts` y cambia la URL del backend:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://tu-api-url.com/v1\"")
```

O para desarrollo local:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/v1\"")
// 10.0.2.2 es localhost para el emulador de Android
```

### 3. Sincronizar dependencias

```bash
./gradlew build
```

### 4. Ejecutar la aplicación

```bash
./gradlew installDebug
```

O desde Android Studio: **Run > Run 'app'**

## 📂 Estructura del Proyecto

```
app/src/main/java/com/health/nutritionai/
├── data/
│   ├── local/
│   │   ├── dao/              # Data Access Objects
│   │   ├── database/         # Room Database
│   │   └── entity/           # Entidades de Room
│   ├── remote/
│   │   ├── api/              # Definición de API (Retrofit)
│   │   ├── dto/              # Data Transfer Objects
│   │   └── interceptor/      # Interceptores HTTP
│   ├── repository/           # Repositorios
│   └── model/                # Modelos de dominio
├── di/                       # Módulos de Hilt
├── ui/
│   ├── camera/               # Pantalla de cámara
│   ├── dashboard/            # Dashboard principal
│   │   └── components/       # Componentes reutilizables
│   ├── history/              # Historial de comidas
│   ├── navigation/           # Configuración de navegación
│   └── theme/                # Tema de la app
├── util/                     # Utilidades
├── MainActivity.kt           # Actividad principal
└── NutritionApp.kt           # Clase Application
```

## 🔧 Configuración Adicional

### Permisos Requeridos

La aplicación requiere los siguientes permisos (ya configurados en AndroidManifest.xml):

- `CAMERA`: Para capturar fotos de comidas
- `INTERNET`: Para comunicación con la API
- `ACCESS_NETWORK_STATE`: Para verificar conectividad
- `WRITE_EXTERNAL_STORAGE` (API ≤28): Para guardar imágenes
- `READ_EXTERNAL_STORAGE` (API ≤32): Para leer imágenes

### Configuración de Red

Para desarrollo local con emulador:

- Backend en `localhost:3000` → usar `http://10.0.2.2:3000/v1`
- Para dispositivo físico → usar IP de tu máquina en la red local

### ProGuard (Release)

El archivo `proguard-rules.pro` ya está configurado. Para builds de producción:

```bash
./gradlew assembleRelease
```

## 🧪 Testing

### Unit Tests

```bash
./gradlew test
```

### UI Tests

```bash
./gradlew connectedAndroidTest
```

## 📝 API Endpoints Utilizados

La app consume los siguientes endpoints del backend:

### Autenticación

- `POST /auth/register` - Registro de usuario
- `POST /auth/login` - Inicio de sesión

### Análisis de Comidas

- `POST /meals/analyze` - Analizar foto de comida
- `GET /meals` - Obtener lista de comidas
- `GET /meals/{id}` - Obtener detalle de comida
- `DELETE /meals/{id}` - Eliminar comida

### Nutrición

- `GET /nutrition/daily?date={date}` - Nutrición diaria
- `GET /nutrition/weekly?startDate={date}` - Nutrición semanal

### Perfil

- `GET /profile` - Obtener perfil
- `PATCH /profile/goals` - Actualizar objetivos

## 🎨 Personalización

### Colores del Tema

Edita `ui/theme/Color.kt` para cambiar los colores:

```kotlin
val Primary = Color(0xFF6750A4)
val Secondary = Color(0xFF625B71)
// ... más colores
```

### Tipografía

Edita `ui/theme/Type.kt` para cambiar fuentes.

## 🐛 Troubleshooting

### Error: "Cannot resolve symbol 'BuildConfig'"

```bash
./gradlew clean
./gradlew build
```

### Error de conexión a la API

- Verifica que el backend esté corriendo
- Revisa la URL en `BuildConfig.API_BASE_URL`
- Para emulador usa `10.0.2.2` en lugar de `localhost`
- Verifica que `android:usesCleartextTraffic="true"` esté en AndroidManifest (solo para desarrollo)

### CameraX no funciona

- Verifica permisos en tiempo de ejecución
- Asegúrate de que el dispositivo/emulador tenga cámara
- Para emulador, habilita cámara virtual en AVD Manager

## 📄 Licencia

[Especificar licencia]

## 👥 Contribuciones

[Instrucciones para contribuir]

## 🔗 Enlaces

- [Documentación de Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [CameraX Guide](https://developer.android.com/training/camerax)
- [Material Design 3](https://m3.material.io/)
