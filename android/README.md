# Nutrition AI - Android

App Android en Kotlin con Jetpack Compose. Saca fotos de comida, las manda al backend y muestra los macros que detecta la IA.

## Arquitectura

MVVM con estas capas:

- **UI**: Jetpack Compose + Material Design 3
- **ViewModel**: StateFlow para estado
- **Repository**: abstrae remote (Retrofit) y local (Room)
- **DI**: Hilt

## Stack

Kotlin, Jetpack Compose, Material 3, Hilt, Coroutines/Flow, Room, DataStore, Retrofit, OkHttp, Gson, CameraX, Coil, Navigation Compose, Accompanist Permissions.

## Lo que ya está andando

- **Dashboard**: calorías y macros del día, progreso contra objetivos, lista de comidas, navegación entre días.
- **Cámara**: captura, envío al backend, resultado del análisis con estados de carga/error.
- **Historial**: lista de comidas, swipe para borrar, detalle por comida.
- **Cache local**: Room con relaciones Meals/Foods, acceso offline.

## Setup

Requisitos: Android Studio Hedgehog+, JDK 17, SDK 26+, Gradle 8.13.2+.

```bash
cd android
```

Configurá la URL en `app/build.gradle.kts`:

```kotlin
// producción
buildConfigField("String", "API_BASE_URL", "\"https://tu-api.com/v1\"")

// emulador (10.0.2.2 = localhost del host)
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/v1\"")
```

```bash
./gradlew build      # sincronizar
./gradlew installDebug   # instalar en dispositivo/emulador
```

## Estructura

```
app/src/main/java/com/health/nutritionai/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── database/
│   │   └── entity/
│   ├── remote/
│   │   ├── api/
│   │   ├── dto/
│   │   └── interceptor/
│   ├── repository/
│   └── model/
├── di/
├── ui/
│   ├── camera/
│   ├── dashboard/components/
│   ├── history/
│   ├── navigation/
│   └── theme/
├── util/
├── MainActivity.kt
└── NutritionApp.kt
```

## Permisos

`CAMERA`, `INTERNET`, `ACCESS_NETWORK_STATE`, `WRITE_EXTERNAL_STORAGE` (API <= 28), `READ_EXTERNAL_STORAGE` (API <= 32). Ya están en el manifest.

## Consume estos endpoints

```
POST   /v1/auth/register
POST   /v1/auth/login
POST   /v1/meals/analyze
GET    /v1/meals
GET    /v1/meals/{id}
DELETE /v1/meals/{id}
GET    /v1/nutrition/daily?date={date}
GET    /v1/nutrition/weekly?startDate={date}
GET    /v1/profile
PATCH  /v1/profile/goals
```

## Tests

```bash
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # UI tests
```

## Problemas comunes

**BuildConfig no resuelve**: `./gradlew clean && ./gradlew build`

**No conecta a la API**: revisá que el backend esté corriendo y que uses `10.0.2.2` en vez de `localhost` si estás en emulador. Para HTTP en dev, asegurate de tener `android:usesCleartextTraffic="true"` en el manifest.

**CameraX no funciona**: verificá permisos en runtime. En emulador, activá la cámara virtual en AVD Manager.

## Licencia

MIT
