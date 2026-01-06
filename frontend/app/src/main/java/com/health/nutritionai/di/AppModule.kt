package com.health.nutritionai.di

import androidx.room.Room
import com.health.nutritionai.data.local.database.AppDatabase
import com.health.nutritionai.data.remote.ApiClient
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.interceptor.AuthInterceptor
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.NutritionRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.ui.auth.AuthViewModel
import com.health.nutritionai.ui.camera.CameraViewModel
import com.health.nutritionai.ui.dashboard.DashboardViewModel
import com.health.nutritionai.ui.history.HistoryViewModel
import com.health.nutritionai.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "nutrition_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<AppDatabase>().mealDao() }
    single { get<AppDatabase>().foodDao() }

    // Network - Auth Interceptor
    single {
        AuthInterceptor {
            // Token provider - obtiene el token guardado
            val prefs = androidContext().getSharedPreferences("nutrition_ai_prefs", android.content.Context.MODE_PRIVATE)
            prefs.getString("auth_token", null)
        }
    }

    // Network - API Service
    single<NutritionApiService> {
        ApiClient.create(get())
    }

    // Repositories
    single { UserRepository(androidContext(), get()) }
    single {
        MealRepository(
            mealDao = get(),
            foodDao = get(),
            apiService = get(),
            userRepository = get()
        )
    }
    single { NutritionRepository(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { CameraViewModel(get()) }
    viewModel { HistoryViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
}

