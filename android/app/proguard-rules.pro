# --- Reglas para Retrofit y Gson (Librerías de red) ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }

# Evita que Gson falle al no encontrar los nombres de los campos
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- IMPORTANTE: TUS MODELOS DE DATOS ---
# Esto le dice a Android: "No cambies el nombre de las clases que están en esta carpeta"
# VERIFICA que esta ruta coincida con donde tienes tus data class
-keep class com.health.nutritionai.data.** { *; }
-keep class com.health.nutritionai.domain.** { *; }

# (Opcional) Si usas una carpeta específica llamada 'models' o 'entity':
-keep class com.health.nutritionai.**.model.** { *; }