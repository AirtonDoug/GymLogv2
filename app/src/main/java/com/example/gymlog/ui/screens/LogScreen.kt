package com.example.gymlog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymlog.models.*
import com.example.gymlog.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Simple date formatter
private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

// Mock log data - Adicionei dados de exercícios para visualização
val mockWorkoutLogState = mutableStateListOf(
    WorkoutLogEntry(
        workoutName = "Treino Full Body",
        startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time,
        durationMillis = TimeUnit.MINUTES.toMillis(55),
        notes = "Me senti bem durante o treino.",
        caloriesBurned = 430,
        performedExercises = mutableListOf(
            PerformedExercise(exerciseId = 1, exerciseName = "Supino Reto", targetSets = 3, targetReps = 10, targetWeight = 80.0, sets = mutableListOf(
                PerformedSet(reps = 10, weight = 80.0, isCompleted = true),
                PerformedSet(reps = 9, weight = 80.0, isCompleted = true),
                PerformedSet(reps = 8, weight = 80.0, isCompleted = true)
            )),
            PerformedExercise(exerciseId = 3, exerciseName = "Agachamento", targetSets = 3, targetReps = 12, targetWeight = 100.0, sets = mutableListOf(
                PerformedSet(reps = 12, weight = 100.0, isCompleted = true),
                PerformedSet(reps = 12, weight = 100.0, isCompleted = true)
            ))
        )
    ),
    WorkoutLogEntry(
        workoutName = "Treino de Pernas",
        startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.time,
        durationMillis = TimeUnit.MINUTES.toMillis(60),
        caloriesBurned = 510,
        performedExercises = mutableListOf(
            PerformedExercise(exerciseId = 8, exerciseName = "Leg Press", targetSets = 4, targetReps = 10, targetWeight = 200.0, sets = mutableListOf(
                PerformedSet(reps = 10, weight = 200.0, isCompleted = true),
                PerformedSet(reps = 10, weight = 220.0, isCompleted = true),
                PerformedSet(reps = 8, weight = 220.0, isCompleted = true),
                PerformedSet(reps = 8, weight = 220.0, isCompleted = true)
            ))
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    navController: NavController,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<WorkoutLogEntry?>(null) }
    var expandedLogId by rememberSaveable { mutableStateOf<String?>(null) } // Estado para controlar o card expandido

    val onDeleteConfirmed = {
        entryToDelete?.let { entry ->
            mockWorkoutLogState.remove(entry)
        }
        showDeleteConfirmationDialog = false
        entryToDelete = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Treino") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Favoritos") }, onClick = { navController.navigate("favorites"); showMenu = false }, leadingIcon = { Icon(Icons.Default.Favorite, null) })
                        DropdownMenuItem(text = { Text("Configurações") }, onClick = { navController.navigate("settings"); showMenu = false }, leadingIcon = { Icon(Icons.Default.Settings, null) })
                        DropdownMenuItem(text = { Text("Ajuda") }, onClick = { navController.navigate("help"); showMenu = false }, leadingIcon = { Icon(Icons.Default.Help, null) })
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("start_workout") }) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Novo Treino")
            }
        }
    ) { innerPadding ->
        if (mockWorkoutLogState.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nenhum treino registrado ainda.", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Clique no '+' para iniciar seu primeiro treino!", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Histórico", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(mockWorkoutLogState, key = { it.id }) { logEntry ->
                    WorkoutLogCard(
                        logEntry = logEntry,
                        isExpanded = expandedLogId == logEntry.id,
                        onExpandClick = {
                            expandedLogId = if (expandedLogId == logEntry.id) null else logEntry.id
                        },
                        onEditClick = {
                            // --- AÇÃO DE NAVEGAÇÃO IMPLEMENTADA ---
                            navController.navigate("edit_workout/${logEntry.id}")
                        },
                        onDelete = {
                            entryToDelete = logEntry
                            showDeleteConfirmationDialog = true
                        }
                    )
                }
            }
        }

        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false; entryToDelete = null },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Tem certeza que deseja excluir este registro de treino? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    Button(onClick = onDeleteConfirmed) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirmationDialog = false; entryToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogCard(
    logEntry: WorkoutLogEntry,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = logEntry.workoutName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    // Botão para Editar
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Botão para Expandir/Recolher
                    IconButton(onClick = onExpandClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Mostrar Detalhes",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Botão de Deletar
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Registro", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data: ${dateFormatter.format(logEntry.startTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(logEntry.durationMillis)
                StatRow(icon = Icons.Default.Timer, label = "Duração", value = "${durationMinutes} min")
                logEntry.caloriesBurned?.let {
                    StatRow(icon = Icons.Default.LocalFireDepartment, label = "Calorias", value = "$it kcal")
                }
            }

            logEntry.notes?.takeIf { it.isNotBlank() }?.let {
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "Notas: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                PerformedExercisesDetails(exercises = logEntry.performedExercises)
            }
        }
    }
}

@Composable
fun PerformedExercisesDetails(exercises: List<PerformedExercise>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Divider()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Exercícios Realizados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (exercises.isEmpty()) {
            Text(
                text = "Nenhum exercício foi registrado para este treino.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        } else {
            exercises.forEach { exercise ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    exercise.sets.forEachIndexed { index, set ->
                        Text(
                            text = "  • Set ${index + 1}: ${set.reps} reps com ${set.weight} kg",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}