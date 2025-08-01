package com.example.gymlog.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gymlog.R
import com.example.gymlog.models.profileData
import com.example.gymlog.ui.components.BottomNavigationBar
import com.example.gymlog.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    val userProfile = profileData

    var userName by remember { mutableStateOf("Carregando...") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Busca os dados do usuário (nome e foto) do Firebase quando a tela é iniciada
    LaunchedEffect(Unit) {
        authViewModel.getUserName { name ->
            userName = name ?: userProfile.name
        }
        authViewModel.getUserPhotoUrl { url ->
            userPhotoUrl = url
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                actions = {
                    // Botão para editar perfil (funcionalidade futura)
                    IconButton(onClick = { /* TODO: Navigate to Edit Profile Screen */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Perfil"
                        )
                    }
                    // Menu de três pontinhos
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Favoritos") },
                            leadingIcon = { Icon(Icons.Default.Favorite, null) },
                            onClick = { navController.navigate("favorites"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Configurações") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = { navController.navigate("settings"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Ajuda") },
                            leadingIcon = { Icon(Icons.Default.Help, null) },
                            onClick = { navController.navigate("help"); showMenu = false }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagem de Perfil carregada da Internet com Coil
            AsyncImage(
                model = userPhotoUrl,
                contentDescription = "Foto de Perfil",
                placeholder = painterResource(id = R.drawable.profile), // Imagem padrão enquanto carrega
                error = painterResource(id = R.drawable.profile),       // Imagem padrão em caso de erro
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nome do Usuário
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card de Detalhes do Perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileDetailItem(icon = Icons.Default.Height, label = "Altura", value = "${userProfile.height / 100} m")
                    Divider()
                    ProfileDetailItem(icon = Icons.Default.FitnessCenter, label = "Peso", value = "${userProfile.weight} kg")
                    Divider()
                    ProfileDetailItem(icon = Icons.Default.Cake, label = "Idade", value = "28 anos")
                    Divider()
                    ProfileDetailItem(icon = Icons.Default.Flag, label = "Objetivo", value = "Ganho de Massa")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card de Estatísticas
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estatísticas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatColumn(value = "15", label = "Treinos Concluídos")
                        StatColumn(value = "120", label = "Horas Treinadas")
                        StatColumn(value = "5", label = "Recordes Pessoais")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de Logout
            OutlinedButton(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sair")
            }
        }
    }
}

// Funções auxiliares para os itens da UI
@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}