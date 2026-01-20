package com.health.nutritionai.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.health.nutritionai.BuildConfig
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val ROOT_URL = BuildConfig.API_BASE_URL.replace("/v1", "")
    private const val BASE_URL = BuildConfig.API_BASE_URL + "/"

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

        val gson: Gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(NutritionApiService::class.java)
    }

    fun getFullImageUrl(relativePath: String): String {
        if (relativePath.startsWith("http")) return relativePath
        val path = if (relativePath.startsWith("/")) relativePath else "/$relativePath"
        return ROOT_URL + path
    }
}
