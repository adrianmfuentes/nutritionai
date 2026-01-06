# Documentación Técnica - Nutrition AI Frontend

## 🏗️ Arquitectura Detallada

### Flujo de Datos

```
UI (Composable)
    ↓ Eventos de usuario
ViewModel
    ↓ Llamadas a repositorio
Repository
    ↓ Llamadas a fuentes de datos
┌─────────────────┬─────────────────┐
│  Remote (API)   │  Local (Room)   │
└─────────────────┴─────────────────┘
```

### Patrón Repository

El patrón Repository actúa como una única fuente de verdad:

1. **MealRepository**: Gestiona comidas y análisis
   - Cache local con Room
   - Sincronización con API
   - Manejo de estados de red

2. **NutritionRepository**: Gestiona estadísticas nutricionales
   - Obtención de datos diarios/semanales
   - Cálculo de progresos

3. **UserRepository**: Gestiona autenticación y perfil
   - Almacenamiento seguro de tokens (DataStore)
   - Gestión de sesión

## 🔄 Estados de UI

### CameraUiState
```kotlin
sealed class CameraUiState {
    object Idle                          // Estado inicial
    object Capturing                     // Capturando foto
    object Analyzing                     // Analizando con IA
    data class Success(meal: Meal)       // Análisis exitoso
    data class Error(message: String)    // Error
}
```

### DashboardUiState
```kotlin
sealed class DashboardUiState {
    object Loading                                // Cargando datos
    data class Success(summary: NutritionSummary) // Datos cargados
    data class Error(message: String)             // Error
}
```

## 🔐 Seguridad

### Autenticación

- **JWT Bearer Tokens**: Almacenados en DataStore encriptado
- **AuthInterceptor**: Añade token automáticamente a requests
- **Refresh Token**: (Pendiente implementación)

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response {
        val token = getTokenFromDataStore()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

### Permisos

Solicitud dinámica de permisos con Accompanist:

```kotlin
val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

if (!cameraPermission.status.isGranted) {
    cameraPermission.launchPermissionRequest()
}
```

## 💾 Base de Datos Local

### Schema

**Tabla: meals**
- mealId (PK): String
- userId: String
- mealType: String
- imageUrl: String
- totalCalories: Int
- totalProtein: Double
- totalCarbs: Double
- totalFat: Double
- timestamp: String

**Tabla: detected_foods**
- id (PK): Long
- mealId (FK): String
- name: String
- confidence: Double
- portionAmount: Double
- portionUnit: String
- calories: Int
- protein: Double
- carbs: Double
- fat: Double
- category: String

### Queries

```kotlin
@Query("SELECT * FROM meals WHERE userId = :userId ORDER BY timestamp DESC")
fun getAllMeals(userId: String): Flow<List<MealEntity>>

@Query("SELECT * FROM meals WHERE userId = :userId AND DATE(timestamp) = :date")
fun getMealsByDate(userId: String, date: String): Flow<List<MealEntity>>
```

## 🌐 Networking

### Retrofit Configuration

```kotlin
Retrofit.Builder()
    .baseUrl(BuildConfig.API_BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

### OkHttp Interceptors

1. **AuthInterceptor**: Añade token de autenticación
2. **LoggingInterceptor**: Logs de requests/responses (solo debug)

### Error Handling

```kotlin
sealed class NetworkResult<T> {
    class Success<T>(data: T) : NetworkResult<T>()
    class Error<T>(message: String) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}
```

## 📸 CameraX Implementation

### Pipeline de Captura

1. **PreviewUseCase**: Vista previa en tiempo real
2. **ImageCaptureUseCase**: Captura de imagen
3. **Procesamiento**: Compresión y optimización
4. **Upload**: Envío multipart a API

```kotlin
val imageCapture = ImageCapture.Builder()
    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
    .build()

imageCapture.takePicture(
    outputOptions,
    executor,
    object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: OutputFileResults) {
            // Procesar imagen
        }
        override fun onError(exception: ImageCaptureException) {
            // Manejar error
        }
    }
)
```

## 🎨 UI Components

### Componentes Reutilizables

**MacroCard**: Tarjeta para mostrar macronutrientes
```kotlin
@Composable
fun MacroCard(
    title: String,
    current: Double,
    goal: Double,
    unit: String,
    color: Color
)
```

**CaloriesCard**: Tarjeta destacada para calorías
**MealCard**: Tarjeta de comida en lista

### Material Design 3

- **Color System**: Tema dinámico con Material You
- **Typography**: Escala tipográfica coherente
- **Components**: Material 3 components (Cards, Buttons, etc.)

## 🔄 Sincronización Offline-First

### Estrategia

1. **Read**: Siempre desde cache local (Flow)
2. **Write**: Guardar local + sync con API
3. **Refresh**: Pull to refresh para actualizar desde API

```kotlin
fun getMealsByDate(userId: String, date: String): Flow<List<Meal>> {
    return mealDao.getMealsByDate(userId, date)
        .map { entities -> entities.map { it.toMeal() } }
}
```

## 📊 Estado y Recomposición

### StateFlow vs LiveData

Se usa **StateFlow** para:
- Compatibilidad con Compose
- Soporte para coroutines nativo
- Operadores funcionales (map, filter, etc.)

```kotlin
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### Recomposición Inteligente

```kotlin
val uiState by viewModel.uiState.collectAsState()

when (uiState) {
    is UiState.Loading -> LoadingScreen()
    is UiState.Success -> SuccessScreen(data)
    is UiState.Error -> ErrorScreen(error)
}
```

## 🧪 Testing Strategy

### Unit Tests

```kotlin
@Test
fun `when meal is analyzed successfully, state should be Success`() = runTest {
    // Arrange
    val meal = createMockMeal()
    coEvery { repository.analyzeMeal(any()) } returns NetworkResult.Success(meal)
    
    // Act
    viewModel.analyzeMeal(mockFile)
    
    // Assert
    assertThat(viewModel.uiState.value).isInstanceOf(CameraUiState.Success::class.java)
}
```

### UI Tests

```kotlin
@Test
fun whenCameraButtonClicked_shouldCaptureImage() {
    composeTestRule.setContent {
        CameraScreen()
    }
    
    composeTestRule.onNodeWithContentDescription("Capturar")
        .performClick()
    
    composeTestRule.onNodeWithText("Analizando...")
        .assertIsDisplayed()
}
```

## 🚀 Performance Optimization

### Image Loading
- Coil con cache en disco y memoria
- Lazy loading en listas
- Placeholder images

### Database
- Índices en columnas frecuentemente consultadas
- Queries optimizadas con LIMIT y OFFSET
- Paginación en listas grandes

### Compose
- `remember` para evitar recreación de objetos
- `LaunchedEffect` para efectos secundarios
- `derivedStateOf` para cálculos derivados

## 📦 Módulos de Hilt

### AppModule
Provee repositorios

### DatabaseModule
Provee Room Database y DAOs

### NetworkModule
Provee Retrofit, OkHttp, interceptores

## 🔮 Mejoras Futuras

### Pendiente de Implementación

1. **Autenticación Completa**
   - Pantalla de login/registro
   - Refresh token
   - Biometría

2. **Funcionalidades Adicionales**
   - Edición de comidas
   - Búsqueda de alimentos
   - Gráficas de progreso
   - Exportar datos
   - Modo oscuro

3. **Performance**
   - Paginación en historial
   - Work Manager para sync en background
   - Image caching strategy

4. **Testing**
   - Cobertura de tests >80%
   - Integration tests
   - Screenshot tests

5. **Accesibilidad**
   - Content descriptions completos
   - Soporte para lectores de pantalla
   - Tamaños de fuente escalables

## 📚 Referencias

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- [Kotlin Coroutines Best Practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)

