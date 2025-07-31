package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.ProfileData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for Profile Screen
data class ProfileUiState(
    val profileData: ProfileData? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false
)

class ProfileViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    // Expose UI state as StateFlow
    val uiState: StateFlow<ProfileUiState> = workoutRepository.getUserProfile()
        .map { profile ->
            ProfileUiState(
                profileData = profile,
                isLoading = false
            )
        }
        .catch { e ->
            emit(ProfileUiState(isLoading = false, errorMessage = "Failed to load profile: ${e.message}"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState(isLoading = true)
        )

    // Function to update profile data
    fun updateProfile(name: String, height: Double, weight: Double) {
        val currentProfile = uiState.value.profileData ?: return

        val updatedProfile = currentProfile.copy(
            name = name,
            height = height,
            weight = weight
        )

        viewModelScope.launch {
            try {
                workoutRepository.updateUserProfile(updatedProfile)
                // Show success message
                _uiState.update { it.copy(updateSuccess = true) }
                // Reset success flag after a delay
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(updateSuccess = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update profile: ${e.message}") }
            }
        }
    }

    // Mutable state for handling UI events
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
}

// Factory for creating ProfileViewModel
class ProfileViewModelFactory(private val repository: WorkoutRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
