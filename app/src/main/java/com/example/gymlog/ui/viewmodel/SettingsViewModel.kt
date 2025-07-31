package com.example.gymlog.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.UserPreferences
import com.example.gymlog.data.repositories.UserPreferencesRepository
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val workoutRepository: WorkoutRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    val userPreferencesFlow: StateFlow<UserPreferences> =
        userPreferencesRepository.userPreferencesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // Valor inicial atualizado para incluir o tema padrão
            initialValue = UserPreferences(
                isDarkMode = false,
                notificationsEnabled = true,
                appTheme = AppTheme.DEFAULT
            )
        )

    /**
     * Define o modo escuro para o aplicativo.
     * @param isDarkMode Verdadeiro para ativar o modo escuro, falso para desativar.
     */
    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDarkMode(isDarkMode)
        }
    }

    /**
     * Define a preferência de notificações do usuário.
     * @param notificationsEnabled Verdadeiro para ativar as notificações, falso para desativar.
     */
    fun setNotificationsEnabled(notificationsEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateNotificationsEnabled(notificationsEnabled)
        }
    }

    /**
     * Define o tema visual do aplicativo.
     * @param appTheme O tema a ser aplicado.
     */
    fun setAppTheme(appTheme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateAppTheme(appTheme)
        }
    }

    /**
     * Limpa todos os treinos marcados como favoritos pelo usuário.
     */
    fun clearFavorites() {
        viewModelScope.launch {
            workoutRepository.clearFavorites()
        }
    }
}