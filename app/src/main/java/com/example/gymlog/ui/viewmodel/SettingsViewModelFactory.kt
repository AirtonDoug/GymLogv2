package com.example.gymlog.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymlog.data.repositories.UserPreferencesRepository
import com.example.gymlog.data.repositories.WorkoutRepository

// A fábrica agora só precisa do WorkoutRepository.
class SettingsViewModelFactory(
    private val application: Application,
    private val workoutRepository: WorkoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            // Aqui, em vez de receber, pedimos a instância única.
            val userPrefsRepo = UserPreferencesRepository.getInstance(application)
            return SettingsViewModel(application, workoutRepository, userPrefsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}