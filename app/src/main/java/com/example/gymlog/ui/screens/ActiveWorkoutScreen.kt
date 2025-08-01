package com.example.gymlog.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymlog.models.*
import com.example.gymlog.utils.RestTimerNotifier
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    navController: NavController,
    workoutIdOrCustom: String
) {
    val isCustomWorkout = workoutIdOrCustom == "custom"
    val routineId = workoutIdOrCustom.toIntOrNull()
    val context = LocalContext.current // Contexto para a notificação

    val workoutRoutine = remember { mockWorkoutRoutines.find { it.id == routineId } }
    val workoutName = workoutRoutine?.name ?: "Treino Personalizado"

    var currentLogEntry by remember {
        mutableStateOf(
            WorkoutLogEntry(
                routineId = routineId,
                workoutName = workoutName,
                startTime = Date(),
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

    var showRestTimerDialog by rememberSaveable { mutableStateOf(false) }
    var restTimerSeconds by rememberSaveable { mutableStateOf(60) }
    var currentRestTime by rememberSaveable { mutableStateOf(0) }
    var isRestTimerRunning by rememberSaveable { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    // Efeito para controlar o timer e disparar a notificação
    LaunchedEffect(isRestTimerRunning, currentRestTime) {
        if (isRestTimerRunning && currentRestTime > 0) {
            delay(1000L)
            currentRestTime--
        } else if (currentRestTime == 0 && isRestTimerRunning) {
            isRestTimerRunning = false
            // --- AÇÃO QUANDO O TIMER TERMINA ---
            RestTimerNotifier.showNotification(context)
        }
    }

    val addSet = { exerciseIndex: Int ->
        val exercises = currentLogEntry.performedExercises.toMutableList()
        if (exerciseIndex in exercises.indices) {
            val exerciseToUpdate = exercises[exerciseIndex]
            val lastSet = exerciseToUpdate.sets.lastOrNull()
            val newSet = PerformedSet(
                reps = lastSet?.reps ?: exerciseToUpdate.targetReps,
                weight = lastSet?.weight ?: exerciseToUpdate.targetWeight
            )
            val updatedSets = exerciseToUpdate.sets.toMutableList().apply { add(newSet) }
            exercises[exerciseIndex] = exerciseToUpdate.copy(sets = updatedSets)
            currentLogEntry = currentLogEntry.copy(performedExercises = exercises)
        }
    }

    val addCustomExercise = { exercise: Exercise ->
        val newPerformedExercise = PerformedExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            targetSets = exercise.sets,
            targetReps = exercise.reps,
            targetWeight = exercise.weight,
            sets = mutableListOf(PerformedSet(reps = exercise.reps, weight = exercise.weight))
        )
        val updatedExercises = currentLogEntry.performedExercises.toMutableList().apply {
            add(newPerformedExercise)
        }
        currentLogEntry = currentLogEntry.copy(performedExercises = updatedExercises)
        showAddExerciseDialog = false
    }

    val updateSet = { exerciseIndex: Int, setIndex: Int, reps: String, weight: String ->
        val exercises = currentLogEntry.performedExercises.toMutableList()
        if (exerciseIndex in exercises.indices) {
            val sets = exercises[exerciseIndex].sets.toMutableList()
            if (setIndex in sets.indices) {
                val currentSet = sets[setIndex]
                sets[setIndex] = currentSet.copy(
                    reps = reps.toIntOrNull() ?: currentSet.reps,
                    weight = weight.toDoubleOrNull() ?: currentSet.weight
                )
                exercises[exerciseIndex] = exercises[exerciseIndex].copy(sets = sets)
                currentLogEntry = currentLogEntry.copy(performedExercises = exercises)
            }
        }
    }

    val toggleSetCompletion = { exerciseIndex: Int, setIndex: Int ->
        val exercises = currentLogEntry.performedExercises.toMutableList()
        if (exerciseIndex in exercises.indices) {
            val sets = exercises[exerciseIndex].sets.toMutableList()
            if (setIndex in sets.indices) {
                val currentSet = sets[setIndex]
                sets[setIndex] = currentSet.copy(isCompleted = !currentSet.isCompleted)
                exercises[exerciseIndex] = exercises[exerciseIndex].copy(sets = sets)
                currentLogEntry = currentLogEntry.copy(performedExercises = exercises)

                if (sets[setIndex].isCompleted) {
                    currentRestTime = restTimerSeconds
                    isRestTimerRunning = true
                    showRestTimerDialog = true
                }
            }
        }
    }

    val finishWorkout = {
        val endTime = Date()
        val duration = endTime.time - currentLogEntry.startTime.time
        val finishedEntry = currentLogEntry.copy(
            endTime = endTime,
            durationMillis = duration
        )
        mockWorkoutLogState.add(0, finishedEntry)
        navController.popBackStack("log", inclusive = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
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

            if (isCustomWorkout && currentLogEntry.performedExercises.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("Treino vazio", style = MaterialTheme.typography.headlineSmall)
                            Text("Clique no '+' para adicionar o primeiro exercício.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

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
                onSubtractTime = { currentRestTime = (currentRestTime - 15).coerceAtLeast(0) }, // <-- Lógica para subtrair
                onSetTotalTime = { restTimerSeconds = it }
            )
        }

        if (showAddExerciseDialog) {
            AddExerciseDialog(
                allExercises = exerciseList,
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
            if (exercise.sets.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("Set", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                    Text("Reps", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                    Text("Peso (kg)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                    Text("Status", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelMedium)
                }
                Divider()
            }

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
    onSubtractTime: () -> Unit, // <-- Novo parâmetro
    onSetTotalTime: (Int) -> Unit
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSetTotalTime(30) }, enabled = !isRunning) { Text("30s") }
                    Button(onClick = { onSetTotalTime(60) }, enabled = !isRunning) { Text("60s") }
                    Button(onClick = { onSetTotalTime(90) }, enabled = !isRunning) { Text("90s") }
                }
            }
        },
        confirmButton = {
            // --- BOTÕES DE CONTROLE ATUALIZADOS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReset, enabled = !isRunning) {
                    Icon(Icons.Default.Refresh, contentDescription = "Resetar")
                }
                // Botão para subtrair 15s
                IconButton(onClick = onSubtractTime, enabled = isRunning) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Subtrair 15s")
                }
                // Botão de Play/Pause
                FilledIconButton(
                    onClick = if (isRunning) onStop else onStart,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pausar" else "Iniciar",
                        modifier = Modifier.size(32.dp)
                    )
                }
                // Botão para adicionar 15s
                IconButton(onClick = onAddTime, enabled = isRunning) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Adicionar 15s")
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
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
