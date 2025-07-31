package com.example.gymlog.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlog.models.WorkoutLogEntry // Use the updated model
import com.example.gymlog.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Simple date formatter
private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

// Mock log data - Make it mutable to allow adding/deleting/editing
val mockWorkoutLogState = mutableStateListOf<WorkoutLogEntry>(
    // Initial mock data (can be adapted from previous LogScreen)
    WorkoutLogEntry(
        workoutName = "Treino Full Body",
        startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1); set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0) }.time,
        endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1); set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 55) }.time,
        durationMillis = TimeUnit.MINUTES.toMillis(55),
        notes = "Me senti bem durante o treino.",
        caloriesBurned = 430
        // performedExercises would be populated here in a real scenario
    ),
    WorkoutLogEntry(
        workoutName = "Treino de Pernas",
        startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3); set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 15) }.time,
        endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3); set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 15) }.time,
        durationMillis = TimeUnit.MINUTES.toMillis(60),
        caloriesBurned = 510
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    navController: NavController,
    // Pass mutable state and actions if state is lifted later
    // workoutLog: List<WorkoutLogEntry>,
    // onStartWorkout: () -> Unit,
    // onEditLog: (WorkoutLogEntry) -> Unit,
    // onDeleteLog: (WorkoutLogEntry) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<WorkoutLogEntry?>(null) }

    // Function to handle deletion confirmation
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
                title = { Text("Registro de Treino") }, // Updated Title
                actions = {
                    // Menu (Favoritos, Configurações, Ajuda)
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
            FloatingActionButton(onClick = { navController.navigate("start_workout") }) { // Navigate to new screen
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
            // Lista de registros de treino (Histórico)
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
                        onEdit = { /* TODO: Navigate to Edit Screen or show Dialog */ },
                        onDelete = {
                            entryToDelete = logEntry
                            showDeleteConfirmationDialog = true
                        }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
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
    onEdit: () -> Unit,
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
                // Edit and Delete Buttons
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Registro", tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                    StatRow(icon = Icons.Default.LocalFireDepartment, label = "Calorias", value = "${it} kcal")
                }
            }

            logEntry.notes?.takeIf { it.isNotBlank() }?.let {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Notas: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // TODO: Optionally display a summary of performed exercises
        }
    }
}

// StatRow composable remains the same as before
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
