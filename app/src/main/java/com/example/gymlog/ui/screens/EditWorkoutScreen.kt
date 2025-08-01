package com.example.gymlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlog.models.WorkoutLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    navController: NavController,
    logId: String?
) {
    // Encontra o treino a ser editado na lista mock. Em um app real, isso viria de um ViewModel.
    val workoutToEdit = remember { mockWorkoutLogState.find { it.id == logId } }

    // Estado para manter as alterações do treino. Começa com os dados do treino encontrado.
    var editableLogEntry by remember { mutableStateOf(workoutToEdit) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Treino", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão para salvar as alterações
                    Button(
                        onClick = {
                            editableLogEntry?.let { updatedEntry ->
                                // Encontra o índice do treino original na lista
                                val index = mockWorkoutLogState.indexOfFirst { it.id == updatedEntry.id }
                                if (index != -1) {
                                    // Substitui o treino antigo pelo novo na lista
                                    mockWorkoutLogState[index] = updatedEntry
                                }
                                // Volta para a tela de log
                                navController.popBackStack()
                            }
                        },
                        enabled = editableLogEntry != null
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Salvar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salvar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (editableLogEntry == null) {
            // Mensagem de erro caso o treino não seja encontrado
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Treino não encontrado.")
            }
        } else {
            // A UI de edição é muito parecida com a de criação
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(editableLogEntry!!.performedExercises) { index, performedExercise ->
                    // Reutilizamos o PerformedExerciseCard da ActiveWorkoutScreen
                    PerformedExerciseCard(
                        exercise = performedExercise,
                        exerciseIndex = index,
                        onAddSet = { exerciseIndex ->
                            // Lógica para adicionar um set (imutável)
                            val updatedExercises = editableLogEntry!!.performedExercises.toMutableList()
                            val exerciseToUpdate = updatedExercises[exerciseIndex]
                            val lastSet = exerciseToUpdate.sets.lastOrNull()
                            val newSet = com.example.gymlog.models.PerformedSet(
                                reps = lastSet?.reps ?: exerciseToUpdate.targetReps,
                                weight = lastSet?.weight ?: exerciseToUpdate.targetWeight
                            )
                            val updatedSets = exerciseToUpdate.sets.toMutableList().apply { add(newSet) }
                            updatedExercises[exerciseIndex] = exerciseToUpdate.copy(sets = updatedSets)
                            editableLogEntry = editableLogEntry!!.copy(performedExercises = updatedExercises)
                        },
                        onUpdateSet = { exerciseIndex, setIndex, reps, weight ->
                            // Lógica para atualizar um set (imutável)
                            val updatedExercises = editableLogEntry!!.performedExercises.toMutableList()
                            val sets = updatedExercises[exerciseIndex].sets.toMutableList()
                            val currentSet = sets[setIndex]
                            sets[setIndex] = currentSet.copy(
                                reps = reps.toIntOrNull() ?: currentSet.reps,
                                weight = weight.toDoubleOrNull() ?: currentSet.weight
                            )
                            updatedExercises[exerciseIndex] = updatedExercises[exerciseIndex].copy(sets = sets)
                            editableLogEntry = editableLogEntry!!.copy(performedExercises = updatedExercises)
                        },
                        onToggleSetCompletion = { exerciseIndex, setIndex ->
                            // Lógica para marcar/desmarcar um set (imutável)
                            val updatedExercises = editableLogEntry!!.performedExercises.toMutableList()
                            val sets = updatedExercises[exerciseIndex].sets.toMutableList()
                            val currentSet = sets[setIndex]
                            sets[setIndex] = currentSet.copy(isCompleted = !currentSet.isCompleted)
                            updatedExercises[exerciseIndex] = updatedExercises[exerciseIndex].copy(sets = sets)
                            editableLogEntry = editableLogEntry!!.copy(performedExercises = updatedExercises)
                        },
                        onStartRestTimer = { /* Timer pode ser desabilitado na edição ou mantido */ }
                    )
                }
            }
        }
    }
}