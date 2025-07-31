package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.WorkoutRoutine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for StartWorkout Screen
data class StartWorkoutUiState(
    val availableRoutines: List<WorkoutRoutine> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class StartWorkoutViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    // Expose UI state as StateFlow
    val uiState: StateFlow<StartWorkoutUiState> = workoutRepository.getWorkoutRoutines()
        .map { routines ->
            StartWorkoutUiState(
                availableRoutines = routines,
                isLoading = false
            )
        }
        .catch { e ->
            emit(StartWorkoutUiState(isLoading = false, errorMessage = "Failed to load workout routines: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StartWorkoutUiState(isLoading = true)
        )

    // Function to start a custom workout
    fun startCustomWorkout() {
        // This is just a navigation trigger, actual navigation happens in the UI
    }

    // Function to start a specific routine
    fun startRoutineWorkout(routineId: Int) {
        // This is just a navigation trigger, actual navigation happens in the UI
    }
}

// Factory for creating StartWorkoutViewModel
class StartWorkoutViewModelFactory(private val repository: WorkoutRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartWorkoutViewModel::class.java)) {
            return StartWorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
