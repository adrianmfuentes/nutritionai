# ----------------------------------------------------------------------------
# REGLAS ESPECÍFICAS PARA NUTRITION AI
# Protege los Modelos de Datos y DTOs para que GSON/Retrofit no fallen al ofuscar
# ----------------------------------------------------------------------------

# 1. DTOs de Autenticación y Perfil (LoginRequest, AuthDto, etc.)
-keep class com.health.nutritionai.data.remote.dto.** { *; }

# 2. Modelos de Dominio (User, UserProfile, etc.)
-keep class com.health.nutritionai.data.model.** { *; }

# 3. Respuestas de API definidas dentro de NutritionApiService (ProfileResponse, etc.)
# Nota: Si las clases están *dentro* del archivo pero fuera de la interface, esta regla las protege.
-keep class com.health.nutritionai.data.remote.api.** { *; }

# ----------------------------------------------------------------------------
# Reglas generales para Retrofit y Gson
# ----------------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }

# Protege los campos que usen @SerializedName aunque la clase se ofusque
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}