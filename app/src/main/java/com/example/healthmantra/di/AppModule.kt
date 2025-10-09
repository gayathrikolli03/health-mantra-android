package com.example.healthmantra.di

import android.content.Context
import androidx.room.Room
import com.example.healthmantra.data.health.HealthConnectManager
import com.example.healthmantra.data.local.AppDatabase
import com.example.healthmantra.data.local.ExerciseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "health_mantra_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    @Singleton
    fun provideHealthConnectManager(
        @ApplicationContext context: Context
    ): HealthConnectManager {
        return HealthConnectManager(context)
    }
}