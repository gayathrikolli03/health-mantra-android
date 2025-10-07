package com.example.healthmantra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.healthmantra.data.model.Exercise

@Database(
    entities = [Exercise::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}