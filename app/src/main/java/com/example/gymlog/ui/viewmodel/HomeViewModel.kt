

package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.WorkoutRoutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Data class to hold the UI state for the Home screen
data class HomeUiState(
    val routines: List<WorkoutRoutine> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val favoritingInProgress: Set<Int> = emptySet() // IDs das rotinas sendo favoritadas
)

class HomeViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoritingInProgress = MutableStateFlow<Set<Int>>(emptySet())

    // Combine routines flow with search query flow to get filtered results
    val uiState: StateFlow<HomeUiState> = combine(
        workoutRepository.getWorkoutRoutines(), // Gets routines with updated favorite status
        _searchQuery,
        _favoritingInProgress
    ) { routines, query, favoritingIds ->
        delay(1000) // Simula o atraso de rede
        val filteredRoutines = if (query.isBlank()) {
            routines
        } else {
            routines.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.difficulty.contains(query, ignoreCase = true)
            }
        }
        HomeUiState(
            routines = filteredRoutines,
            searchQuery = query,
            isLoading = false, // Loading is done
            favoritingInProgress = favoritingIds
        )
    }.catch { e ->
        // Handle errors, e.g., update state with error message
        emit(HomeUiState(isLoading = false, errorMessage = "Failed to load routines: ${e.message}"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep flow active for 5s after last subscriber
        initialValue = HomeUiState(isLoading = true) // Initial loading state
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(routine: WorkoutRoutine) {
        viewModelScope.launch {
            _favoritingInProgress.update { it + routine.id }
            try {
                delay(1000) // Simula o atraso de rede
                workoutRepository.toggleFavorite(routine.id)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _favoritingInProgress.update { it - routine.id }
            }
        }
    }
}