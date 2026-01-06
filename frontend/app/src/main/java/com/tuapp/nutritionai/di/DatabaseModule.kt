package com.tuapp.nutritionai.di

import android.content.Context
import androidx.room.Room
import com.tuapp.nutritionai.data.local.dao.MealDao
import com.tuapp.nutritionai.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nutrition_app_db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideMealDao(appDatabase: AppDatabase): MealDao {
        return appDatabase.mealDao()
    }
}
