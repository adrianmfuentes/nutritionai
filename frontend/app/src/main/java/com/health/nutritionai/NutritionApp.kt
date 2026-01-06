package com.health.nutritionai

import android.app.Application
import com.health.nutritionai.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NutritionApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NutritionApp)
            modules(appModule)
        }
    }
}

