package com.tuapp.nutritionai.data.remote.api

import com.tuapp.nutritionai.data.remote.dto.AnalyzeMealResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface NutritionApiService {

    @Multipart
    @POST("meals/analyze")
    suspend fun analyzeMeal(
        @Part image: MultipartBody.Part
    ): Response<AnalyzeMealResponse>

    // Add other endpoints as needed
    // @GET("meals")
    // suspend fun getMeals(): Response<List<AnalyzeMealResponse>>
}
