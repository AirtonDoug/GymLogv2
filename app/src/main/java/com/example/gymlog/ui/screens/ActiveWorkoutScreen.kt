package com.example.gymlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymlog.models.*
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    navController: NavController,
    workoutIdOrCustom: String // Can be routine ID (Int as String) or "custom"
) {
    val isCustomWorkout = workoutIdOrCustom == "custom"
    val routineId = workoutIdOrCustom.toIntOrNull()

    // Find the routine or set up for custom
    val workoutRoutine = remember { mockWorkoutRoutines.find { it.id == routineId } }
    val workoutName = workoutRoutine?.name ?: "Treino Personalizado"

    // State for the ongoing workout log entry
    var currentLogEntry by remember {
        mutableStateOf(
            WorkoutLogEntry(
                routineId = routineId,
                workoutName = workoutName,
                startTime = Date(),
                // Initialize with exercises from routine or empty for custom
                performedExercises = workoutRoutine?.exercises?.map { exercise ->
                    PerformedExercise(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        targetSets = exercise.sets,
                        targetReps = exercise.reps,
                        targetWeight = exercise.weight
                    )
                }?.toMutableList() ?: mutableListOf()
            )
        )
    }

    // State for the Rest Timer
    var showRestTimerDialog by rememberSaveable { mutableStateOf(false) }
    var restTimerSeconds by rememberSaveable { mutableStateOf(60) } // Default rest time
    var currentRestTime by rememberSaveable { mutableStateOf(0) }
    var isRestTimerRunning by rememberSaveable { mutableStateOf(false) }

    // State for adding custom exercises
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    // Timer Coroutine
    LaunchedEffect(isRestTimerRunning, currentRestTime) {
        if (isRestTimerRunning && currentRestTime > 0) {
            delay(1000L)
            currentRestTime--
        } else if (currentRestTime == 0 && isRestTimerRunning) {
            isRestTimerRunning = false
            // Optionally add a sound/vibration feedback here
        }
    }

    // Function to add a set to an exercise
    val addSet = { exerciseIndex: Int ->
        val exercise = currentLogEntry.performedExercises[exerciseIndex]
        val lastSet = exercise.sets.lastOrNull()
        exercise.sets.add(
            PerformedSet(
                reps = lastSet?.reps ?: exercise.targetReps,
                weight = lastSet?.weight ?: exercise.targetWeight
            )
        )
        // Trigger recomposition by creating a new list
        currentLogEntry = currentLogEntry.copy(performedExercises = currentLogEntry.performedExercises.toMutableList())
    }

    // Function to update a set
    val updateSet = { exerciseIndex: Int, setIndex: Int, reps: String, weight: String ->
        val exercise = currentLogEntry.performedExercises[exerciseIndex]
        val set = exercise.sets[setIndex]
        set.reps = reps.toIntOrNull() ?: set.reps
        set.weight = weight.toDoubleOrNull() ?: set.weight
        // Trigger recomposition
        currentLogEntry = currentLogEntry.copy(performedExercises = currentLogEntry.performedExercises.toMutableList())
    }

    // Function to toggle set completion
    val toggleSetCompletion = { exerciseIndex: Int, setIndex: Int ->
        val exercise = currentLogEntry.performedExercises[exerciseIndex]
        val set = exercise.sets[setIndex]
        set.isCompleted = !set.isCompleted
        // Trigger recomposition
        currentLogEntry = currentLogEntry.copy(performedExercises = currentLogEntry.performedExercises.toMutableList())

        // Start rest timer if set is completed
        if (set.isCompleted) {
            currentRestTime = restTimerSeconds
            isRestTimerRunning = true
            showRestTimerDialog = true
        }
    }

    // Function to finish workout
    val finishWorkout = {
        val endTime = Date()
        val duration = endTime.time - currentLogEntry.startTime.time
        val finishedEntry = currentLogEntry.copy(
            endTime = endTime,
            durationMillis = duration
            // TODO: Calculate calories burned if possible
        )
        mockWorkoutLogState.add(0, finishedEntry) // Add to the beginning of the log
        navController.popBackStack("log", inclusive = false) // Go back to log screen
    }

    // Function to add a custom exercise
    val addCustomExercise = { exercise: Exercise ->
        currentLogEntry.performedExercises.add(
            PerformedExercise(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                targetSets = exercise.sets,
                targetReps = exercise.reps,
                targetWeight = exercise.weight
            )
        )
        // Add one initial set
        addSet(currentLogEntry.performedExercises.lastIndex)
        showAddExerciseDialog = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Add confirmation dialog before exiting */ navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Finish Workout Button
                    Button(onClick = { finishWorkout() }) {
                        Text("Finalizar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isCustomWorkout) {
                FloatingActionButton(onClick = { showAddExerciseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Exercício")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(currentLogEntry.performedExercises, key = { _, item -> item.id }) { index, performedExercise ->
                PerformedExerciseCard(
                    exercise = performedExercise,
                    exerciseIndex = index,
                    onAddSet = addSet,
                    onUpdateSet = updateSet,
                    onToggleSetCompletion = toggleSetCompletion,
                    onStartRestTimer = {
                        currentRestTime = restTimerSeconds
                        isRestTimerRunning = true
                        showRestTimerDialog = true
                    }
                )
            }

            // Button to add exercise in custom mode (alternative to FAB)
            if (isCustomWorkout) {
                item {
                    OutlinedButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar Exercício")
                    }
                }
            }
        }

        // Rest Timer Dialog
        if (showRestTimerDialog) {
            RestTimerDialog(
                totalSeconds = restTimerSeconds,
                currentTime = currentRestTime,
                isRunning = isRestTimerRunning,
                onDismiss = { showRestTimerDialog = false },
                onStop = { isRestTimerRunning = false },
                onStart = { isRestTimerRunning = true; if(currentRestTime == 0) currentRestTime = restTimerSeconds },
                onReset = { currentRestTime = restTimerSeconds; isRestTimerRunning = false },
                onAddTime = { currentRestTime += 15 },
                onSetTotalTime = { restTimerSeconds = it }
            )
        }

        // Add Exercise Dialog (for custom workouts)
        if (showAddExerciseDialog) {
            AddExerciseDialog(
                allExercises = exerciseList, // Provide the master list
                onDismiss = { showAddExerciseDialog = false },
                onExerciseSelected = { addCustomExercise(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformedExerciseCard(
    exercise: PerformedExercise,
    exerciseIndex: Int,
    onAddSet: (Int) -> Unit,
    onUpdateSet: (Int, Int, String, String) -> Unit,
    onToggleSetCompletion: (Int, Int) -> Unit,
    onStartRestTimer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(exercise.exerciseName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Header Row
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Text("Set", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                Text("Reps", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("Peso (kg)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("Status", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelMedium)
            }
            Divider()

            // Sets List
            exercise.sets.forEachIndexed { setIndex, set ->
                PerformedSetRow(
                    set = set,
                    setNumber = setIndex + 1,
                    onUpdate = { reps, weight -> onUpdateSet(exerciseIndex, setIndex, reps, weight) },
                    onToggleCompletion = { onToggleSetCompletion(exerciseIndex, setIndex) }
                )
                Divider()
            }

            // Add Set Button
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { onAddSet(exerciseIndex) }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Set")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Set")
                }
                IconButton(onClick = onStartRestTimer) {
                    Icon(Icons.Default.Timer, contentDescription = "Iniciar Timer Descanso")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformedSetRow(
    set: PerformedSet,
    setNumber: Int,
    onUpdate: (String, String) -> Unit,
    onToggleCompletion: () -> Unit
) {
    var reps by remember(set.reps) { mutableStateOf(set.reps.toString()) }
    var weight by remember(set.weight) { mutableStateOf(set.weight.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(setNumber.toString(), modifier = Modifier.weight(0.5f))

        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it; onUpdate(it, weight) },
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),

        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it; onUpdate(reps, it) },
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),

        )

        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = { onToggleCompletion() },
            modifier = Modifier.weight(0.8f)
        )
    }
}

@Composable
fun RestTimerDialog(
    totalSeconds: Int,
    currentTime: Int,
    isRunning: Boolean,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onAddTime: () -> Unit,
    onSetTotalTime: (Int) -> Unit // Allow changing the default rest time
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer de Descanso") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(currentTime),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = if (totalSeconds > 0) currentTime.toFloat() / totalSeconds else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Quick add time buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSetTotalTime(30) }, enabled = !isRunning) { Text("30s") }
                    Button(onClick = { onSetTotalTime(60) }, enabled = !isRunning) { Text("60s") }
                    Button(onClick = { onSetTotalTime(90) }, enabled = !isRunning) { Text("90s") }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(onClick = onReset, enabled = !isRunning) {
                    Icon(Icons.Default.Refresh, contentDescription = "Resetar")
                }
                IconButton(onClick = if (isRunning) onStop else onStart) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pausar" else "Iniciar"
                    )
                }
                IconButton(onClick = onAddTime, enabled = isRunning) {
                    Icon(Icons.Default.AddAlarm, contentDescription = "Adicionar 15s")
                }
                TextButton(onClick = onDismiss) {
                    Text("Fechar")
                }
            }
        }
    )
}

@Composable
fun AddExerciseDialog(
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredExercises = remember(searchQuery) {
        allExercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Exercício") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar exercício...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(filteredExercises, key = { it.id }) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExerciseSelected(exercise) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Optional: Add exercise image thumbnail
                            Text(exercise.name, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Add, contentDescription = "Adicionar")
                        }
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Helper function to format time
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
