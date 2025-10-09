package com.example.healthmantra.data.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.healthmantra.data.model.Exercise
import com.example.healthmantra.data.model.ExerciseSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    suspend fun isAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun readExercises(): List<Exercise> {
        Log.d("HealthConnect", "=== Starting readExercises ===")

        if (!isAvailable()) {
            Log.e("HealthConnect", "Health Connect NOT available")
            return emptyList()
        }
        Log.d("HealthConnect", " Health Connect IS available")

        if (!hasAllPermissions()) {
            Log.e("HealthConnect", " NO permissions granted")
            return emptyList()
        }
        Log.d("HealthConnect", " Permissions ARE granted")

        return try {
            val now = Instant.now()
            val thirtyDaysAgo = now.minusSeconds(30 * 24 * 60 * 60)

            Log.d("HealthConnect", " Reading from $thirtyDaysAgo to $now")

            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(thirtyDaysAgo, now)
            )

            val response = healthConnectClient.readRecords(request)
            Log.d("HealthConnect", "Found ${response.records.size} total records from Health Connect")

            if (response.records.isEmpty()) {
                Log.w("HealthConnect", "⚠ No records found in Health Connect!")
                return emptyList()
            }

            val currentTime = System.currentTimeMillis()
            val oneDayAgo = currentTime - (24 * 60 * 60 * 1000L)

            val exercises = response.records.mapNotNull { record ->
                try {
                    val durationMinutes = calculateDuration(record)
                    val exerciseDate = record.startTime.toEpochMilli()
                    val exerciseName = record.title ?: "Exercise"

                    val isFuture = exerciseDate >= currentTime
                    val isWithinLastDay = exerciseDate >= oneDayAgo

                    Log.d("HealthConnect", "🏃 Record: $exerciseName")
                    Log.d("HealthConnect", "   Duration: $durationMinutes min")
                    Log.d("HealthConnect", "   Date: ${java.util.Date(exerciseDate)}")
                    Log.d("HealthConnect", "   Future: $isFuture, Within 24h: $isWithinLastDay")

                    if (isWithinLastDay) {
                        Log.d("HealthConnect", "    Including this exercise")
                        Exercise(
                            name = exerciseName,
                            duration = durationMinutes,
                            calories = estimateCalories(durationMinutes),
                            date = exerciseDate,
                            source = ExerciseSource.GOOGLE_HEALTH
                        )
                    } else {
                        Log.d("HealthConnect", "    Skipping (too old)")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("HealthConnect", " Error processing record: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            Log.d("HealthConnect", " Returning ${exercises.size} exercises to app")
            exercises

        } catch (e: Exception) {
            Log.e("HealthConnect", " Error reading exercises: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun calculateDuration(record: ExerciseSessionRecord): Int {
        val durationMillis = record.endTime.toEpochMilli() - record.startTime.toEpochMilli()
        return (durationMillis / 60000).toInt()
    }

    private fun estimateCalories(durationMinutes: Int): Int {
        // Simple estimation: ~5 calories per minute of exercise
        return durationMinutes * 5
    }
}