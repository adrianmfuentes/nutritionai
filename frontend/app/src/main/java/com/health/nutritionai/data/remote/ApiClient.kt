package com.health.nutritionai.data.remote

import com.health.nutritionai.BuildConfig
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // ⚠️ CONFIGURACIÓN DE URL DEL BACKEND
    // Cambia esta URL según tu entorno:
    //
    // Para emulador Android Studio:
    //   private const val BASE_URL = "http://10.0.2.2:3000/v1/"
    //
    // Para dispositivo físico (mismo WiFi que tu PC):
    //   private const val BASE_URL = "http://TU_IP_LOCAL:3000/v1/"
    //   Ejemplo: "http://192.168.1.101:3000/v1/"
    //   Para encontrar tu IP: En Windows ejecuta 'ipconfig' en CMD
    //
    // Para producción:
    //   private const val BASE_URL = "https://tu-dominio.com/v1/"

    private const val BASE_URL = "http://api-nutricion.amfserver.duckdns.com/v1/"

    fun create(authInterceptor: AuthInterceptor): NutritionApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(NutritionApiService::class.java)
    }
}
