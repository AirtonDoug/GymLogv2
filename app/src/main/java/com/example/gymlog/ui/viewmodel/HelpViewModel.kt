package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.FAQ
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for Help Screen
data class HelpUiState(
    val faqs: List<FAQ> = emptyList(),
    val expandedFaqIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val supportMessageSent: Boolean = false
)

class HelpViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    // Mutable state for help screen
    private val _uiState = MutableStateFlow(
        HelpUiState(
            isLoading = true
        )
    )

    // Expose UI state as StateFlow
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    init {
        loadFaqs()
    }

    private fun loadFaqs() {
        viewModelScope.launch {
            try {
                // In a real app, you would load FAQs from repository
                // For now, we're using the hardcoded list from the model
                _uiState.update { it.copy(
                    faqs = com.example.gymlog.models.faqList,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load FAQs: ${e.message}"
                    )
                }
            }
        }
    }

    // Toggle FAQ expansion
    fun toggleFaqExpansion(faqId: Int) {
        _uiState.update { currentState ->
            val expandedIds = currentState.expandedFaqIds.toMutableSet()
            if (faqId in expandedIds) {
                expandedIds.remove(faqId)
            } else {
                expandedIds.add(faqId)
            }
            currentState.copy(expandedFaqIds = expandedIds)
        }
    }

    // Send support message
    fun sendSupportMessage(name: String, email: String, message: String) {
        viewModelScope.launch {
            try {
                // In a real app, you would send this to a backend
                // For now, just simulate success
                _uiState.update { it.copy(supportMessageSent = true) }

                // Reset after a delay
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(supportMessageSent = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to send message: ${e.message}") }
            }
        }
    }
}

// Factory for creating HelpViewModel
class HelpViewModelFactory(private val repository: WorkoutRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HelpViewModel::class.java)) {
            return HelpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
