package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.WorkoutRoutine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for Search Screen
data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<WorkoutRoutine> = emptyList(),
    val isLoading: Boolean = false, // Adicionado o estado de carregamento
    val errorMessage: String? = null
)

class SearchViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    // Mutable state for search query
    private val _searchQuery = MutableStateFlow("")

    // Expose UI state as StateFlow
    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        workoutRepository.getWorkoutRoutines()
    ) { query, routines ->
        if (query.isBlank()) {
            // Se a busca estiver vazia, não mostre nada e não carregue
            return@combine SearchUiState(searchQuery = query, isLoading = false)
        }

        // Inicia o carregamento
        _uiState.update { it.copy(isLoading = true) }
        delay(1500) // Simula o atraso da busca na rede

        val filteredRoutines = routines.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.difficulty.contains(query, ignoreCase = true)
        }

        SearchUiState(
            searchQuery = query,
            searchResults = filteredRoutines,
            isLoading = false // Finaliza o carregamento
        )
    }.catch { e ->
        emit(SearchUiState(errorMessage = "Failed to search: ${e.message}", isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState() // Estado inicial sem carregamento
    )

    // Update search query
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Quando a busca muda, o 'combine' será reativado, iniciando o processo de carregamento
        if (query.isNotBlank()) {
            // Atualiza o estado para indicar o início do carregamento
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    // Toggle favorite status for a routine
    fun toggleFavorite(routine: WorkoutRoutine) {
        viewModelScope.launch {
            try {
                workoutRepository.toggleFavorite(routine.id)
                // UI will update automatically via the StateFlow
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    // Mutable state para o fluxo de UI
    private val _uiState = MutableStateFlow(SearchUiState())
}