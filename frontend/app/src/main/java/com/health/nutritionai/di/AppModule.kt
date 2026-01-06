package com.health.nutritionai.di

import androidx.room.Room
import com.health.nutritionai.data.local.database.AppDatabase
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.ui.camera.CameraViewModel
import com.health.nutritionai.ui.dashboard.DashboardViewModel
import com.health.nutritionai.ui.history.HistoryViewModel
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

    // Repositories (offline mode - no API)
    single { MealRepository(get(), get()) }
    single { UserRepository(androidContext()) }

    // ViewModels
    viewModel { DashboardViewModel(get()) }
    viewModel { CameraViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
}

