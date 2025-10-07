package com.example.healthmantra.data.repository

import com.example.healthmantra.data.local.ExerciseDao
import com.example.healthmantra.data.model.ConflictGroup
import com.example.healthmantra.data.model.Exercise
import com.example.healthmantra.data.model.ExerciseSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {

    fun getAllExercises(): Flow<List<Exercise>> {
        // Only get exercises from today onwards
        val startOfToday = getStartOfToday()
        return exerciseDao.getAllExercises().map { exercises ->
            exercises.filter { it.date >= startOfToday }
        }
    }

    fun getConflictGroups(): Flow<List<ConflictGroup>> {
        return exerciseDao.getConflictedExercises().map { exercises ->
            val startOfToday = getStartOfToday()
            exercises.filter { it.date >= startOfToday }
                .groupBy { it.conflictGroupId }
                .filter { it.key != null }
                .map { (groupId, groupExercises) ->
                    ConflictGroup(
                        id = groupId!!,
                        exercises = groupExercises,
                        date = groupExercises.minOf { it.date }
                    )
                }
                .sortedBy { it.date } // Sort by date ascending (upcoming first)
        }
    }

    suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
        cleanupPastExercises()
        detectConflicts()
    }

    suspend fun syncFromGoogleHealth() {
        val existingExercises = exerciseDao.getAllExercises().first()
        val hasGoogleHealthData = existingExercises.any { it.source == ExerciseSource.GOOGLE_HEALTH }

        if (hasGoogleHealthData) {
            detectConflicts()
            return
        }

        val currentTime = System.currentTimeMillis()

        // Create exercises that are always in the future
        val googleExercises = listOf(
            Exercise(
                name = "Morning Yoga",
                duration = 30,
                calories = 150,
                date = currentTime + (2 * 60 * 60 * 1000L), // 2 hours from now
                source = ExerciseSource.GOOGLE_HEALTH
            ),
            Exercise(
                name = "Evening Run",
                duration = 45,
                calories = 400,
                date = currentTime + (4 * 60 * 60 * 1000L), // 4 hours from now
                source = ExerciseSource.GOOGLE_HEALTH
            ),
            Exercise(
                name = "Swimming",
                duration = 60,
                calories = 500,
                date = currentTime + (24 * 60 * 60 * 1000L), // Tomorrow same time
                source = ExerciseSource.GOOGLE_HEALTH
            )
        )
        exerciseDao.insertExercises(googleExercises)
        cleanupPastExercises()
        detectConflicts()
    }
    suspend fun detectConflictsManually() {
        detectConflicts()
    }

    private suspend fun detectConflicts() {
        val allExercises = exerciseDao.getAllExercises().first()
        val startOfToday = getStartOfToday()
        val todayAndFuture = allExercises.filter { it.date >= startOfToday }

        // Clear existing conflicts
        todayAndFuture.filter { it.isConflicted }.forEach { exercise ->
            exerciseDao.updateExercise(
                exercise.copy(isConflicted = false, conflictGroupId = null)
            )
        }

        val conflicts = mutableListOf<List<Exercise>>()
        val processed = mutableSetOf<Long>()

        for (exercise in todayAndFuture) {
            if (exercise.id in processed) continue

            val exerciseStart = exercise.date
            val exerciseEnd = exercise.date + (exercise.duration * 60 * 1000L)

            val overlapping = todayAndFuture.filter { other ->
                if (other.id == exercise.id || other.id in processed) return@filter false

                val otherStart = other.date
                val otherEnd = other.date + (other.duration * 60 * 1000L)

                (exerciseStart < otherEnd && exerciseEnd > otherStart)
            }

            if (overlapping.isNotEmpty()) {
                conflicts.add(listOf(exercise) + overlapping)
                processed.add(exercise.id)
                processed.addAll(overlapping.map { it.id })
            }
        }

        conflicts.forEach { group ->
            val groupId = UUID.randomUUID().toString()
            group.forEach { exercise ->
                exerciseDao.updateExercise(
                    exercise.copy(isConflicted = true, conflictGroupId = groupId)
                )
            }
        }
    }

    private suspend fun cleanupPastExercises() {
        val startOfToday = getStartOfToday()
        val allExercises = exerciseDao.getAllExercises().first()
        val pastExercises = allExercises.filter { it.date < startOfToday }

        if (pastExercises.isNotEmpty()) {
            exerciseDao.deleteExercisesByIds(pastExercises.map { it.id })
        }
    }

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    suspend fun resolveConflict(keepExercise: Exercise, conflictGroupId: String) {
        val allExercises = exerciseDao.getAllExercises().first()
        val conflictGroup = allExercises.filter { it.conflictGroupId == conflictGroupId }
        val toDelete = conflictGroup.filter { it.id != keepExercise.id }

        exerciseDao.deleteExercisesByIds(toDelete.map { it.id })
        exerciseDao.updateExercise(
            keepExercise.copy(isConflicted = false, conflictGroupId = null)
        )
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise)
        detectConflicts()
    }
}