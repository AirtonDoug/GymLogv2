package com.example.gymlog.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gymlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Adicionamos um construtor privado para que só a própria classe se possa criar.
class UserPreferencesRepository private constructor(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: false
            val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            val appTheme = AppTheme.valueOf(
                preferences[PreferencesKeys.APP_THEME] ?: AppTheme.DEFAULT.name
            )
            UserPreferences(isDarkMode, notificationsEnabled, appTheme)
        }

    suspend fun updateDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = isDarkMode
        }
    }

    suspend fun updateAppTheme(appTheme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = appTheme.name
        }
    }

    suspend fun updateNotificationsEnabled(notificationsEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = notificationsEnabled
        }
    }

    // O "companion object" gere a instância única (Singleton).
    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            // Se a instância já existir, retorna-a.
            // Se não, cria uma nova de forma segura.
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferencesRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

// A data class não muda.
data class UserPreferences(
    val isDarkMode: Boolean,
    val notificationsEnabled: Boolean,
    val appTheme: AppTheme
)