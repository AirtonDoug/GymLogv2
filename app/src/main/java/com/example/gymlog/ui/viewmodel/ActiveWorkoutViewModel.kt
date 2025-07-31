package com.example.gymlog.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.data.repositories.WorkoutRepository
import com.example.gymlog.models.Exercise
import com.example.gymlog.models.PerformedExercise
import com.example.gymlog.models.PerformedSet
import com.example.gymlog.models.WorkoutLogEntry
import com.example.gymlog.models.WorkoutRoutine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// UI State for ActiveWorkout Screen
data class ActiveWorkoutUiState(
    val workoutRoutine: WorkoutRoutine? = null,
    val isCustomWorkout: Boolean = false,
    val currentExercises: List<PerformedExercise> = emptyList(),
    val timerRunning: Boolean = false,
    val timerSeconds: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ActiveWorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // Get workoutIdOrCustom from navigation arguments
    private val workoutIdOrCustom: String = savedStateHandle.get<String>("workoutIdOrCustom") ?: "custom"
    private val isCustom = workoutIdOrCustom == "custom"

    // Mutable state for the active workout
    private val _uiState = MutableStateFlow(
        ActiveWorkoutUiState(
            isCustomWorkout = isCustom,
            isLoading = true
        )
    )

    // Expose UI state as StateFlow
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    // Timer state
    private var timerJob: kotlinx.coroutines.Job? = null

    // Track workout start time
    private var workoutStartTime: Date = Date()

    init {
        loadWorkout()
        workoutStartTime = Date() // Record start time
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            try {
                if (isCustom) {
                    // For custom workout, start with empty exercise list
                    _uiState.update { it.copy(
                        isLoading = false,
                        isCustomWorkout = true,
                        currentExercises = emptyList()
                    )}
                } else {
                    // For routine-based workout, load the routine
                    val routineId = workoutIdOrCustom.toIntOrNull() ?: -1
                    workoutRepository.getWorkoutRoutineById(routineId).collect { routine ->
                        if (routine != null) {
                            // Convert routine exercises to performed exercises
                            val performedExercises = routine.exercises.map { exercise ->
                                PerformedExercise(
                                    exerciseId = exercise.id,
                                    exerciseName = exercise.name,
                                    sets = mutableListOf(),
                                    targetSets = exercise.sets,
                                    targetReps = exercise.reps,
                                    targetWeight = exercise.weight
                                )
                            }

                            _uiState.update { it.copy(
                                isLoading = false,
                                workoutRoutine = routine,
                                isCustomWorkout = false,
                                currentExercises = performedExercises
                            )}
                        } else {
                            _uiState.update { it.copy(
                                isLoading = false,
                                errorMessage = "Routine not found"
                            )}
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load workout: ${e.message}"
                )}
            }
        }
    }

    // Add a new exercise to custom workout
    fun addExercise(exercise: Exercise) {
        val performedExercise = PerformedExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            sets = mutableListOf(),
            targetSets = exercise.sets,
            targetReps = exercise.reps,
            targetWeight = exercise.weight
        )

        _uiState.update { currentState ->
            currentState.copy(
                currentExercises = currentState.currentExercises + performedExercise
            )
        }
    }

    // Add a set to an exercise
    fun addSet(exerciseIndex: Int, weight: Double, reps: Int) {
        val currentExercises = _uiState.value.currentExercises.toMutableList()
        if (exerciseIndex in currentExercises.indices) {
            val exercise = currentExercises[exerciseIndex]
            val newSet = PerformedSet(
                id = UUID.randomUUID().toString(),
                reps = reps,
                weight = weight,
                isCompleted = true
            )

            val updatedSets = exercise.sets.toMutableList().apply {
                add(newSet)
            }

            currentExercises[exerciseIndex] = exercise.copy(sets = updatedSets)

            _uiState.update { it.copy(currentExercises = currentExercises) }
        }
    }

    // Update a set
    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: Double, reps: Int, completed: Boolean) {
        val currentExercises = _uiState.value.currentExercises.toMutableList()
        if (exerciseIndex in currentExercises.indices) {
            val exercise = currentExercises[exerciseIndex]
            val sets = exercise.sets.toMutableList()

            if (setIndex in sets.indices) {
                val updatedSet = sets[setIndex].copy(
                    weight = weight,
                    reps = reps,
                    isCompleted = completed
                )
                sets[setIndex] = updatedSet

                currentExercises[exerciseIndex] = exercise.copy(sets = sets)
                _uiState.update { it.copy(currentExercises = currentExercises) }
            }
        }
    }

    // Remove a set
    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        val currentExercises = _uiState.value.currentExercises.toMutableList()
        if (exerciseIndex in currentExercises.indices) {
            val exercise = currentExercises[exerciseIndex]
            val sets = exercise.sets.toMutableList()

            if (setIndex in sets.indices) {
                sets.removeAt(setIndex)
                currentExercises[exerciseIndex] = exercise.copy(sets = sets)
                _uiState.update { it.copy(currentExercises = currentExercises) }
            }
        }
    }

    // Start/stop rest timer
    fun toggleTimer() {
        if (_uiState.value.timerRunning) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerRunning = true, timerSeconds = 60) } // Default 60 seconds

        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0 && _uiState.value.timerRunning) {
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }

            if (_uiState.value.timerSeconds <= 0) {
                _uiState.update { it.copy(timerRunning = false) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerRunning = false) }
    }

    // Reset timer to a specific value
    fun resetTimer(seconds: Int) {
        stopTimer()
        _uiState.update { it.copy(timerSeconds = seconds) }
    }

    // Complete workout and save to log
    fun completeWorkout(notes: String = "") {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val endTime = Date()
                val durationMillis = endTime.time - workoutStartTime.time

                // Create log entry
                val logEntry = WorkoutLogEntry(
                    id = UUID.randomUUID().toString(),
                    routineId = currentState.workoutRoutine?.id,
                    workoutName = currentState.workoutRoutine?.name ?: "Treino Personalizado",
                    startTime = workoutStartTime,
                    endTime = endTime,
                    durationMillis = durationMillis,
                    performedExercises = currentState.currentExercises.toMutableList(),
                    notes = notes,
                    caloriesBurned = currentState.workoutRoutine?.caloriesBurned
                )

                // Save to repository
                workoutRepository.saveWorkoutLog(logEntry)

                // Navigation to log screen happens in UI
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save workout: ${e.message}") }
            }
        }
    }
}

// Factory for creating ActiveWorkoutViewModel
class ActiveWorkoutViewModelFactory(
    private val savedStateHandle: SavedStateHandle,
    private val repository: WorkoutRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveWorkoutViewModel::class.java)) {
            return ActiveWorkoutViewModel(savedStateHandle, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
