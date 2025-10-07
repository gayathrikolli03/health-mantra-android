package com.example.healthmantra.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmantra.data.model.Exercise
import com.example.healthmantra.data.model.ExerciseSource
import com.example.healthmantra.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val conflictCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repository.getAllExercises().collect { exercises ->
                _uiState.update { it.copy(
                    exercises = exercises,
                    isLoading = false,
                    conflictCount = exercises.count { ex -> ex.isConflicted }
                )}
            }
        }
    }

    fun addExercise(name: String, duration: Int, calories: Int, scheduledTime: Long) {
        viewModelScope.launch {
            val exercise = Exercise(
                name = name,
                duration = duration,
                calories = calories,
                date = scheduledTime,
                source = ExerciseSource.MANUAL
            )
            repository.addExercise(exercise)
        }
    }

    fun syncFromGoogleHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                repository.syncFromGoogleHealth()
            } catch (e: Exception) {
                // Log error
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }
}