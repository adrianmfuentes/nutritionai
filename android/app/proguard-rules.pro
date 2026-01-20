# ---------------------------------------------------------
# REGLA MAESTRA (SOLUCIÓN NUCLEAR)
# Mantiene el código dentro de tu paquete com.health.nutritionai
# Esto evita que se rompan los nombres de variables o clases en TU código.
# ---------------------------------------------------------
-keep class com.health.nutritionai.** { *; }

# ---------------------------------------------------------
# Reglas obligatorias para librerías (Déjalas tal cual)
# ---------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }

# Evita problemas con Gson y los nombres serializados
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}