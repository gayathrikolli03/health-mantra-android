package com.example.healthmantra.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExerciseSource {
    MANUAL,
    GOOGLE_HEALTH,
    SAMSUNG_HEALTH,
    GARMIN
}

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val duration: Int, // in minutes
    val calories: Int,
    val date: Long, // timestamp
    val source: ExerciseSource,
    val isConflicted: Boolean = false,
    val conflictGroupId: String? = null
)

data class ConflictGroup(
    val id: String,
    val exercises: List<Exercise>,
    val date: Long
) {
    fun getTimeDifference(): Long {
        if (exercises.size < 2) return 0
        val sortedTimes = exercises.map { it.date }.sorted()
        return sortedTimes.last() - sortedTimes.first()
    }
}