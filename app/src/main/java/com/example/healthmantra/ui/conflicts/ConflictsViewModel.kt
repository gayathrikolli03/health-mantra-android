package com.example.healthmantra.ui.conflicts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmantra.data.model.ConflictGroup
import com.example.healthmantra.data.model.Exercise
import com.example.healthmantra.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConflictsUiState(
    val conflictGroups: List<ConflictGroup> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ConflictsViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConflictsUiState())
    val uiState: StateFlow<ConflictsUiState> = _uiState.asStateFlow()

    init {
        loadConflicts()
    }

    private fun loadConflicts() {
        viewModelScope.launch {
            repository.getConflictGroups().collect { conflicts ->
                _uiState.update { it.copy(
                    conflictGroups = conflicts,
                    isLoading = false
                )}
            }
        }
    }

    fun resolveConflict(exercise: Exercise, conflictGroupId: String) {
        viewModelScope.launch {
            repository.resolveConflict(exercise, conflictGroupId)
        }
    }
}