package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.WorkoutLogEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// UI State for Log Screen
data class LogUiState(
    val logEntries: List<WorkoutLogEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class LogViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    // Expose UI state as StateFlow
    val uiState: StateFlow<LogUiState> = workoutRepository.getWorkoutLogs()
        .map { logs ->
            LogUiState(
                logEntries = logs,
                isLoading = false
            )
        }
        .catch { e ->
            emit(LogUiState(isLoading = false, errorMessage = "Failed to load workout logs: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LogUiState(isLoading = true)
        )

    // Function to delete a log entry
    fun deleteLogEntry(logId: String) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkoutLog(logId)
                // The UI will update automatically via the StateFlow
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    // Function to start a new workout
    fun navigateToStartWorkout() {
        // This is just a navigation trigger, actual navigation happens in the UI
    }
}

// Factory for creating LogViewModel
class LogViewModelFactory(private val repository: WorkoutRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
            return LogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
