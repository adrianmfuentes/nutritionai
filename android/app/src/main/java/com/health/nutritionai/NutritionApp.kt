package com.health.nutritionai

import android.app.Application
import android.content.Context
import android.util.Log
import com.health.nutritionai.di.appModule
import com.health.nutritionai.util.LocaleHelper
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
