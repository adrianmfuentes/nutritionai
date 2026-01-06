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

    // ⚠️ IMPORTANTE: Cambia esta URL para pruebas locales
    // Para emulador: "http://10.0.2.2:3000/v1/"
    // Para dispositivo físico: "http://TU_IP_LOCAL:3000/v1/" (ej: "http://192.168.1.100:3000/v1/")
    // Para producción: usa BuildConfig.API_BASE_URL
    private const val BASE_URL = "http://192.168.1.101:3000/v1/"

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
