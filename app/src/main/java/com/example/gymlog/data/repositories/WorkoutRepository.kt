package com.example.gymlog.data.repositories

import com.example.gymlog.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID

/**
 * Extended interface for accessing workout data including logs
 */
interface WorkoutRepository {
    // Workout Routines
    fun getWorkoutRoutines(): Flow<List<WorkoutRoutine>>
    fun getWorkoutRoutineById(id: Int): Flow<WorkoutRoutine?>
    fun getFavoriteWorkoutRoutines(): Flow<List<WorkoutRoutine>>
    suspend fun toggleFavorite(routineId: Int)
    suspend fun clearFavorites()

    // Exercises
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExerciseById(id: Int): Flow<Exercise?>

    // Workout Logs
    fun getWorkoutLogs(): Flow<List<WorkoutLogEntry>>
    fun getWorkoutLogById(id: String): Flow<WorkoutLogEntry?>
    suspend fun saveWorkoutLog(logEntry: WorkoutLogEntry)
    suspend fun updateWorkoutLog(logEntry: WorkoutLogEntry)
    suspend fun deleteWorkoutLog(logId: String)

    // User Profile
    fun getUserProfile(): Flow<ProfileData>
    suspend fun updateUserProfile(profileData: ProfileData)

    // User Settings
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateDarkThemeSetting(enabled: Boolean)
    suspend fun updateNotificationsSetting(enabled: Boolean)
    suspend fun updateRestTimerDuration(seconds: Int)
    suspend fun resetUserSettings()
}

/**
 * User settings data class
 */
data class UserSettings(
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val restTimerDuration: Int = 60
)

/**
 * Mock implementation of WorkoutRepository using in-memory data.
 */
class MockWorkoutRepository : WorkoutRepository {

    // Workout Routines
    private val _routines = MutableStateFlow(mockWorkoutRoutines)
    private val _favoriteIds = MutableStateFlow(
        mockWorkoutRoutines.filter { it.isFavorite }.map { it.id }.toSet()
    )

    // Exercises
    private val _exercises = MutableStateFlow(exerciseList)

    // Workout Logs
    private val _workoutLogs = MutableStateFlow<List<WorkoutLogEntry>>(
        listOf(
            // Sample workout log
            WorkoutLogEntry(
                id = UUID.randomUUID().toString(),
                routineId = 1,
                workoutName = "Treino Full Body",
                startTime = Date(System.currentTimeMillis() - 86400000), // Yesterday
                endTime = Date(System.currentTimeMillis() - 86400000 + 3600000), // 1 hour later
                durationMillis = 3600000,
                performedExercises = mutableListOf(
                    PerformedExercise(
                        exerciseId = 1,
                        exerciseName = "Supino Reto",
                        sets = mutableListOf(
                            PerformedSet(reps = 12, weight = 100.0, isCompleted = true),
                            PerformedSet(reps = 10, weight = 100.0, isCompleted = true),
                            PerformedSet(reps = 8, weight = 100.0, isCompleted = true)
                        ),
                        targetSets = 3,
                        targetReps = 12,
                        targetWeight = 100.0
                    )
                ),
                notes = "Bom treino, consegui completar todas as séries.",
                caloriesBurned = 450
            )
        )
    )

    // User Profile
    private val _userProfile = MutableStateFlow(profileData)

    // User Settings
    private val _userSettings = MutableStateFlow(UserSettings())

    // Workout Routines Implementation
    override fun getWorkoutRoutines(): Flow<List<WorkoutRoutine>> {
        return _routines.combine(_favoriteIds) { routines, favIds ->
            routines.map { it.copy(isFavorite = it.id in favIds) }
        }
    }

    override fun getWorkoutRoutineById(id: Int): Flow<WorkoutRoutine?> {
        return getWorkoutRoutines().map { routines -> routines.find { it.id == id } }
    }

    override fun getFavoriteWorkoutRoutines(): Flow<List<WorkoutRoutine>> {
        return getWorkoutRoutines().map { routines -> routines.filter { it.isFavorite } }
    }

    override suspend fun toggleFavorite(routineId: Int) {
        _favoriteIds.update { currentIds ->
            if (routineId in currentIds) {
                currentIds - routineId
            } else {
                currentIds + routineId
            }
        }
    }

    override suspend fun clearFavorites() {
        _favoriteIds.value = emptySet()
    }

    // Exercises Implementation
    override fun getAllExercises(): Flow<List<Exercise>> {
        return _exercises
    }

    override fun getExerciseById(id: Int): Flow<Exercise?> {
        return _exercises.map { exercises -> exercises.find { it.id == id } }
    }

    // Workout Logs Implementation
    override fun getWorkoutLogs(): Flow<List<WorkoutLogEntry>> {
        return _workoutLogs
    }

    override fun getWorkoutLogById(id: String): Flow<WorkoutLogEntry?> {
        return _workoutLogs.map { logs -> logs.find { it.id == id } }
    }

    override suspend fun saveWorkoutLog(logEntry: WorkoutLogEntry) {
        _workoutLogs.update { currentLogs -> currentLogs + logEntry }
    }

    override suspend fun updateWorkoutLog(logEntry: WorkoutLogEntry) {
        _workoutLogs.update { currentLogs ->
            currentLogs.map { if (it.id == logEntry.id) logEntry else it }
        }
    }

    override suspend fun deleteWorkoutLog(logId: String) {
        _workoutLogs.update { currentLogs -> currentLogs.filter { it.id != logId } }
    }

    // User Profile Implementation
    override fun getUserProfile(): Flow<ProfileData> {
        return _userProfile
    }

    override suspend fun updateUserProfile(profileData: ProfileData) {
        _userProfile.value = profileData
    }

    // User Settings Implementation
    override fun getUserSettings(): Flow<UserSettings> {
        return _userSettings
    }

    override suspend fun updateDarkThemeSetting(enabled: Boolean) {
        _userSettings.update { it.copy(isDarkTheme = enabled) }
    }

    override suspend fun updateNotificationsSetting(enabled: Boolean) {
        _userSettings.update { it.copy(notificationsEnabled = enabled) }
    }

    override suspend fun updateRestTimerDuration(seconds: Int) {
        _userSettings.update { it.copy(restTimerDuration = seconds) }
    }

    override suspend fun resetUserSettings() {
        _userSettings.value = UserSettings()
    }
}

// Helper function for combining flows
fun <T1, T2, R> Flow<T1>.combine(flow: Flow<T2>, transform: suspend (T1, T2) -> R): Flow<R> {
    return kotlinx.coroutines.flow.combine(this, flow, transform)
}
