package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.WorkoutRoutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for Favorites Screen
data class FavoritesUiState(
    val favoriteRoutines: List<WorkoutRoutine> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class FavoritesViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    // Expose UI state as StateFlow
    val uiState: StateFlow<FavoritesUiState> = workoutRepository.getFavoriteWorkoutRoutines()
        .map { favorites ->
            delay(1500)
            FavoritesUiState(
                favoriteRoutines = favorites,
                isLoading = false
            )
        }
        .catch { e ->
            emit(FavoritesUiState(isLoading = false, errorMessage = "Failed to load favorites: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState(isLoading = true)
        )

    // Remove a routine from favorites
    fun removeFavorite(routine: WorkoutRoutine) {
        viewModelScope.launch {
            try {
                workoutRepository.toggleFavorite(routine.id)
                // UI will update automatically via the StateFlow
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }
}

// Factory for creating FavoritesViewModel
class FavoritesViewModelFactory(private val repository: WorkoutRepository = com.example.gymlog.data.repositories.MockWorkoutRepository()) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
