package com.example.healthmantra.data.local

import androidx.room.*
import com.example.healthmantra.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY date DESC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE isConflicted = 1 ORDER BY date DESC")
    fun getConflictedExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id IN (:ids)")
    suspend fun deleteExercisesByIds(ids: List<Long>)

    @Query("SELECT * FROM exercises WHERE date BETWEEN :startTime AND :endTime")
    suspend fun getExercisesInTimeRange(startTime: Long, endTime: Long): List<Exercise>
}