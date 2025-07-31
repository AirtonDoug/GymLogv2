package com.example.gymlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymlog.models.WorkoutRoutine // Corrected import

// Note: SearchBar might be better placed in a common 'components' package

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar rotinas...", // Use WorkoutRoutine context
    onSearch: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isExpanded by remember { mutableStateOf(false) } // State for expanded search view

    // This expanded search logic might be complex for just a component.
    // Consider simplifying or moving the expanded view to a dedicated SearchScreen.
    if (isExpanded) {
        // Full-screen search view (Consider moving to a dedicated screen)
        Surface(
            modifier = Modifier.fillMaxSize(), // Takes full screen
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        isExpanded = false
                        onSearchQueryChange("") // Clear query on exit
                        keyboardController?.hide()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text(placeholderText) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            // Mapeamento das cores da borda (chamada de 'indicator' no TextFieldDefaults.colors)
                            focusedIndicatorColor = Color.Transparent,    // Equivalente a focusedBorderColor
                            unfocusedIndicatorColor = Color.Transparent,  // Equivalente a unfocusedBorderColor
                            disabledIndicatorColor = Color.Transparent, // Para consistência, se quiser borda desabilitada transparente

                            // Mapeamento da cor do container/fundo
                            // Para ter o container transparente em todos os estados:
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent, // Garante transparência quando desabilitado
                            errorContainerColor = Color.Transparent     // Garante transparência em estado de erro
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            onSearch()
                            keyboardController?.hide()
                        }),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, "Limpar busca")
                                }
                            }
                        }
                    )
                }
                Divider()
                // Search suggestions (optional)
                // Consider fetching suggestions dynamically
                val suggestions = listOf("Força", "Cardio", "Iniciante", "Avançado", "Pernas", "Braços", "Costas")
                    .filter { it.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    items(suggestions) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSearchQueryChange(suggestion)
                                    onSearch()
                                    isExpanded = false // Close expanded view after selection
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = suggestion)
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    } else {
        // Compact search bar (e.g., inside TopAppBar)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = modifier,
            placeholder = { Text(placeholderText) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, "Limpar busca")
                    }
                } else {
                    // Optional: Icon to expand search
                    /* IconButton(onClick = { isExpanded = true }) {
                       Icon(Icons.Default.Tune, "Expandir busca")
                   } */
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch()
                keyboardController?.hide()
            })
        )
    }
}

// This screen assumes navigation passes the filtered list.
// Alternatively, it could receive the query and perform filtering internally.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    navController: NavController,
    searchQuery: String,
    searchResults: List<WorkoutRoutine>, // Corrected type
    isLoading: Boolean, // Adicionado o parâmetro isLoading
    onSearchQueryChange: (String) -> Unit, // Needed if search bar is part of this screen
    favoriteRoutineIds: Set<Int>,
    onToggleFavorite: (WorkoutRoutine) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados da Busca") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // ... (SearchBar opcional aqui)

            if (isLoading) {
                // Exibe o indicador de progresso no centro da tela
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isEmpty()) {
                // Mensagem de "Nenhum resultado"
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhum resultado encontrado", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tente buscar por outra palavra-chave", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Exibe a lista de resultados
                Text(
                    text = "Resultados para \"$searchQuery\"",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ... (código da lista de resultados)
                }
            }
        }
    }
}
@Composable
fun SearchResultItem(
    workout: WorkoutRoutine, // Corrected type
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (workout.category) {
            "Força" -> Icons.Default.FitnessCenter
            "Cardio" -> Icons.Default.DirectionsRun
            else -> Icons.Default.SportsGymnastics
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(workout.name, style = MaterialTheme.typography.titleSmall)
            Text("${workout.duration} min • ${workout.difficulty}", style = MaterialTheme.typography.bodySmall)
        }

        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
