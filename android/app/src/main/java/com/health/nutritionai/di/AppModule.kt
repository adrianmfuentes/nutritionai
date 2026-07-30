package com.health.nutritionai.di

import androidx.room.Room
import com.health.nutritionai.data.local.database.AppDatabase
import com.health.nutritionai.data.remote.ApiClient
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.interceptor.AuthInterceptor
import com.health.nutritionai.data.remote.interceptor.TokenAuthenticator
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.NutritionRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.ui.auth.AuthViewModel
import com.health.nutritionai.ui.barcode.BarcodeViewModel
import com.health.nutritionai.ui.camera.CameraViewModel
import com.health.nutritionai.ui.chat.ChatViewModel
import com.health.nutritionai.ui.dashboard.DashboardViewModel
import com.health.nutritionai.ui.history.HistoryViewModel
import com.health.nutritionai.ui.settings.SettingsViewModel
import com.health.nutritionai.ui.textinput.TextInputViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "nutrition_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    // DAOs
    single { get<AppDatabase>().mealDao() }
    single { get<AppDatabase>().foodDao() }
    single { get<AppDatabase>().pendingMealDao() }

    // Network - Auth Interceptor
    single {
        val context = androidContext()
        val prefs = context.getSharedPreferences("nutrition_ai_prefs", android.content.Context.MODE_PRIVATE)

        AuthInterceptor(
            tokenProvider = {
                // Token provider - obtiene el token guardado
                prefs.getString("auth_token", null)
            }
        )
    }

    // Network - Token Authenticator (refresca el access token en un 401)
    single {
        val context = androidContext()
        TokenAuthenticator(
            context = context,
            onSessionExpired = {
                // Refresh failed too: clear the session so the user is sent back to login
                android.util.Log.w("TokenAuthenticator", "Session expired, clearing auth data")
                val prefs = context.getSharedPreferences("nutrition_ai_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().remove("auth_token").remove("refresh_token").remove("user_id").apply()
            }
        )
    }

    // Network - API Service
    single<NutritionApiService> {
        ApiClient.create(get(), get())
    }

    // Repositories
    single { UserRepository(androidContext(), get()) }
    single {
        MealRepository(
            context = androidContext(),
            mealDao = get(),
            foodDao = get(),
            pendingMealDao = get(),
            apiService = get(),
            userRepository = get()
        )
    }
    single { NutritionRepository(androidContext(), get()) }

    // ViewModels
    viewModel { AuthViewModel(get(), androidContext() as android.app.Application) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { CameraViewModel(get(), androidContext() as android.app.Application) }
    viewModel { BarcodeViewModel(get(), androidContext() as android.app.Application) }
    viewModel { TextInputViewModel(get(), androidContext() as android.app.Application) }
    viewModel { ChatViewModel(androidContext() as android.app.Application, get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), androidContext() as android.app.Application) }
    viewModel { SettingsViewModel(get(), androidContext() as android.app.Application) }
}
