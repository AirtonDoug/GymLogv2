package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.WorkoutRoutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for WorkoutDetail Screen
data class WorkoutDetailUiState(
    val routine: WorkoutRoutine? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class WorkoutDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // Get workoutId from navigation arguments
    private val workoutId: Int = savedStateHandle.get<Int>("workoutId") ?: -1

    // Mutable state for workout details
    private val _uiState = MutableStateFlow(
        WorkoutDetailUiState(
            isLoading = true
        )
    )

    // Expose UI state as StateFlow
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()

    init {
        loadWorkoutDetails()
    }

    private fun loadWorkoutDetails() {
        viewModelScope.launch {
            try {
                delay(1500)
                workoutRepository.getWorkoutRoutineById(workoutId).collect { routine ->
                    if (routine != null) {
                        _uiState.update {
                            it.copy(
                                routine = routine,
                                isFavorite = routine.isFavorite,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Workout routine not found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load workout details: ${e.message}"
                    )
                }
            }
        }
    }

    // Toggle favorite status
    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                workoutRepository.toggleFavorite(workoutId)
                // UI will update via the StateFlow when the repository emits new data
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update favorite status: ${e.message}") }
            }
        }
    }
}

// Factory for creating WorkoutDetailViewModel
class WorkoutDetailViewModelFactory(
    private val workoutId: Int,
    private val repository: WorkoutRepository = com.example.gymlog.data.repositories.MockWorkoutRepository()
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java)) {
            val fakeSavedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
            return WorkoutDetailViewModel(fakeSavedStateHandle, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
