# Integración Backend con Frontend Android

## Configuración en Android

### 1. Añadir Permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### 2. Configurar Retrofit en build.gradle.kts

```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
}
```

### 3. Crear API Service

```kotlin
// ApiConfig.kt
object ApiConfig {
    private const val BASE_URL = "https://tu-dominio.com/v1/" // Cambiar por tu URL
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

### 4. Definir Data Models

```kotlin
// Models.kt
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class AuthResponse(
    val message: String,
    val user: User,
    val token: String
)

data class User(
    val id: String,
    val email: String,
    val name: String
)

data class MealAnalysisResponse(
    val mealId: String,
    val detectedFoods: List<DetectedFood>,
    val totalNutrition: Nutrition,
    val imageUrl: String,
    val timestamp: String,
    val notes: String
)

data class DetectedFood(
    val name: String,
    val confidence: Double,
    val portion: Portion,
    val nutrition: Nutrition
)

data class Portion(
    val amount: Double,
    val unit: String
)

data class Nutrition(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double?
)

data class DailySummaryResponse(
    val date: String,
    val totals: Nutrition,
    val goals: NutritionGoals,
    val progress: Progress,
    val meals: List<MealSummary>
)

data class NutritionGoals(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class Progress(
    val caloriesPercent: Int,
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int
)

data class MealSummary(
    val id: String,
    val meal_type: String,
    val total_calories: Int,
    val total_protein: Double,
    val total_carbs: Double,
    val total_fat: Double,
    val consumed_at: String,
    val image_url: String,
    val health_score: Double?
)
```

### 5. Definir API Interface

```kotlin
// ApiService.kt
interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @GET("profile")
    suspend fun getProfile(@Header("Authorization") token: String): User
    
    // Meals
    @Multipart
    @POST("meals/analyze")
    suspend fun analyzeMeal(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("mealType") mealType: RequestBody? = null,
        @Part("timestamp") timestamp: RequestBody? = null
    ): MealAnalysisResponse
    
    @GET("meals")
    suspend fun getMeals(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): MealsResponse
    
    @GET("meals/{mealId}")
    suspend fun getMealById(
        @Header("Authorization") token: String,
        @Path("mealId") mealId: String
    ): MealDetailResponse
    
    @DELETE("meals/{mealId}")
    suspend fun deleteMeal(
        @Header("Authorization") token: String,
        @Path("mealId") mealId: String
    ): DeleteResponse
    
    // Nutrition
    @GET("nutrition/daily")
    suspend fun getDailySummary(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null
    ): DailySummaryResponse
    
    @GET("nutrition/weekly")
    suspend fun getWeeklySummary(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String? = null
    ): WeeklySummaryResponse
    
    @PUT("nutrition/goals")
    suspend fun updateGoals(
        @Header("Authorization") token: String,
        @Body goals: UpdateGoalsRequest
    ): UpdateGoalsResponse
}

data class MealsResponse(val meals: List<MealSummary>, val pagination: Pagination)
data class MealDetailResponse(val meal: MealDetail)
data class DeleteResponse(val success: Boolean, val message: String)
data class WeeklySummaryResponse(val days: List<DayNutrition>, val averages: Nutrition, val trend: String)
data class UpdateGoalsRequest(val dailyCalories: Int, val proteinGrams: Int, val carbsGrams: Int, val fatGrams: Int)
data class UpdateGoalsResponse(val goals: NutritionGoals, val message: String)

data class Pagination(val total: Int, val limit: Int, val offset: Int, val pages: Int)
data class MealDetail(val id: String, val meal_type: String, val foods: List<DetectedFood>)
data class DayNutrition(val date: String, val calories: Int, val protein: Double, val carbs: Double, val fat: Double, val mealCount: Int, val healthScore: Double?)
```

### 6. Crear Repository

```kotlin
// NutritionRepository.kt
class NutritionRepository {
    private val apiService = ApiConfig.apiService
    private val sharedPreferences = // Obtener SharedPreferences
    
    private fun getToken(): String {
        return "Bearer ${sharedPreferences.getString("auth_token", "")}"
    }
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            // Guardar token
            sharedPreferences.edit().putString("auth_token", response.token).apply()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun analyzeMeal(imageFile: File, mealType: String? = null): Result<MealAnalysisResponse> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            val mealTypeBody = mealType?.let { 
                it.toRequestBody("text/plain".toMediaTypeOrNull()) 
            }
            
            val response = apiService.analyzeMeal(getToken(), imagePart, mealTypeBody)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDailySummary(date: String? = null): Result<DailySummaryResponse> {
        return try {
            val response = apiService.getDailySummary(getToken(), date)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Más métodos según necesites...
}
```

### 7. Crear ViewModel

```kotlin
// NutritionViewModel.kt
class NutritionViewModel : ViewModel() {
    private val repository = NutritionRepository()
    
    private val _analysisResult = MutableLiveData<Result<MealAnalysisResponse>>()
    val analysisResult: LiveData<Result<MealAnalysisResponse>> = _analysisResult
    
    private val _dailySummary = MutableLiveData<Result<DailySummaryResponse>>()
    val dailySummary: LiveData<Result<DailySummaryResponse>> = _dailySummary
    
    fun analyzeMeal(imageFile: File, mealType: String? = null) {
        viewModelScope.launch {
            _analysisResult.value = repository.analyzeMeal(imageFile, mealType)
        }
    }
    
    fun loadDailySummary(date: String? = null) {
        viewModelScope.launch {
            _dailySummary.value = repository.getDailySummary(date)
        }
    }
}
```

### 8. Uso en Activity/Fragment

```kotlin
class MainActivity : AppCompatActivity() {
    private val viewModel: NutritionViewModel by viewModels()
    private lateinit var imageCapture: ImageCapture
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupObservers()
        setupCamera()
    }
    
    private fun setupObservers() {
        viewModel.analysisResult.observe(this) { result ->
            result.onSuccess { response ->
                // Mostrar resultados
                Toast.makeText(this, "Comida analizada: ${response.totalNutrition.calories} cal", Toast.LENGTH_SHORT).show()
                // Actualizar UI con response.detectedFoods, etc.
            }.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.dailySummary.observe(this) { result ->
            result.onSuccess { summary ->
                // Actualizar UI con resumen diario
                updateDashboard(summary)
            }
        }
    }
    
    private fun captureAndAnalyze() {
        val photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Enviar a analizar
                    viewModel.analyzeMeal(photoFile, "lunch")
                }
                
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Error capturando foto", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
```

## Ejemplo de Flujo Completo

### 1. Login

```kotlin
// En LoginActivity
btnLogin.setOnClickListener {
    val email = etEmail.text.toString()
    val password = etPassword.text.toString()
    
    viewModel.login(email, password)
}

viewModel.loginResult.observe(this) { result ->
    result.onSuccess { authResponse ->
        // Guardar token y navegar a MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }.onFailure { error ->
        showError(error.message)
    }
}
```

### 2. Capturar y Analizar Comida

```kotlin
// En MainActivity
btnCapture.setOnClickListener {
    captureAndAnalyze()
}

private fun captureAndAnalyze() {
    // Capturar foto con CameraX
    imageCapture.takePicture(/* ... */)
    
    // Enviar a backend
    viewModel.analyzeMeal(photoFile)
}

viewModel.analysisResult.observe(this) { result ->
    result.onSuccess { response ->
        // Mostrar alimentos detectados
        adapter.submitList(response.detectedFoods)
        
        // Mostrar totales
        tvCalories.text = "${response.totalNutrition.calories} kcal"
        tvProtein.text = "${response.totalNutrition.protein}g"
    }
}
```

### 3. Ver Resumen Diario

```kotlin
// En DashboardFragment
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    viewModel.loadDailySummary(today)
}

viewModel.dailySummary.observe(viewLifecycleOwner) { result ->
    result.onSuccess { summary ->
        // Actualizar progress bars
        progressCalories.progress = summary.progress.caloriesPercent
        progressProtein.progress = summary.progress.proteinPercent
        
        // Mostrar totales vs objetivos
        tvCalories.text = "${summary.totals.calories} / ${summary.goals.calories} kcal"
    }
}
```

## Configuración de Red para Desarrollo Local

Si estás probando con el backend en localhost:

### Opción 1: Emulador Android Studio
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/v1/"
```

### Opción 2: Dispositivo Físico (mismo WiFi)
```kotlin
private const val BASE_URL = "http://192.168.1.XXX:3000/v1/" // IP de tu PC
```

### Opción 3: Producción
```kotlin
private const val BASE_URL = "https://tu-dominio.com/v1/"
```

## Network Security Config (para HTTP en desarrollo)

`res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.XXX</domain>
    </domain-config>
</network-security-config>
```

`AndroidManifest.xml`:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

## Notas Importantes

1. **Siempre usa HTTPS en producción**
2. **Almacena el token de forma segura** (EncryptedSharedPreferences)
3. **Maneja errores de red apropiadamente**
4. **Implementa retry logic para requests fallidos**
5. **Usa Coil o Glide para cargar imágenes desde URLs**
6. **Implementa caché offline si es necesario**

## Testing

```kotlin
// Test con Postman primero
// Luego integra en Android

// Ejemplo de request con Postman:
POST https://tu-dominio.com/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```
