package com.example.gymlog.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gymlog.data.repositories.AuthRepository // Supondo que você moveu para cá
import com.example.gymlog.data.repositories.MockWorkoutRepository
import com.example.gymlog.ui.screens.*
import com.example.gymlog.ui.viewmodel.AuthViewModel // Supondo que você moveu para cá
import com.example.gymlog.ui.viewmodel.AuthViewModelFactory // Supondo que você moveu para cá
import com.example.gymlog.ui.viewmodel.FavoritesViewModel
import com.example.gymlog.ui.viewmodel.HomeViewModel
import com.example.gymlog.ui.viewmodel.SearchViewModel
import com.example.gymlog.ui.viewmodel.WorkoutDetailViewModel

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    // --- Instâncias dos Repositórios e ViewModels ---

    // Repositório de Autenticação
    val authRepository = remember { AuthRepository() }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))

    // Repositório principal do App
    val repository = remember { MockWorkoutRepository() }

    // Define a rota inicial baseada no estado de login
    val startDestination = if (authRepository.isUserLoggedIn()) "home" else "login"

    // --- Container de Navegação Principal ---

    NavHost(navController = navController, startDestination = startDestination) {

        // --- Rotas de Autenticação ---

        composable("login") {
            LoginScreen(viewModel = authViewModel, navController = navController)
        }

        composable("register") {
            RegisterScreen(viewModel = authViewModel, navController = navController)
        }

        composable("forgotPassword") {
            ForgotPasswordScreen(viewModel = authViewModel, navController = navController)
        }

        // --- Rotas Principais do Aplicativo ---

        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return HomeViewModel(repository) as T
                    }
                }
            )
            HomeScreen(navController = navController, homeViewModel = homeViewModel)
        }

        composable("log") {
            LogScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("start_workout") {
            StartWorkoutScreen(navController = navController)
        }

        // --- ROTA DE EDIÇÃO ATUALIZADA ---
        composable(
            route = "edit_workout/{logId}",
            arguments = listOf(navArgument("logId") { type = NavType.StringType })
        ) { backStackEntry ->
            val logId = backStackEntry.arguments?.getString("logId")
            // Agora aponta para a nova EditWorkoutScreen
            EditWorkoutScreen(
                navController = navController,
                logId = logId
            )
        }


        composable(
            route = "active_workout/{workoutIdOrCustom}",
            arguments = listOf(navArgument("workoutIdOrCustom") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutIdOrCustom = backStackEntry.arguments?.getString("workoutIdOrCustom") ?: "custom"
            ActiveWorkoutScreen(
                navController = navController,
                workoutIdOrCustom = workoutIdOrCustom
            )
        }

        composable(
            route = "workout_details/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getInt("workoutId") ?: -1
            val detailViewModel: WorkoutDetailViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
                        return WorkoutDetailViewModel(savedStateHandle, repository) as T
                    }
                }
            )
            WorkoutDetailScreen(
                navController = navController,
                workoutId = workoutId,
                detailViewModel = detailViewModel
            )
        }

        composable("favorites") {
            val favoritesViewModel: FavoritesViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return FavoritesViewModel(repository) as T
                    }
                }
            )
            FavoritesScreen(navController = navController, favoritesViewModel = favoritesViewModel)
        }

        composable("settings") {
            SettingsScreen(
                navController = navController
            )
        }

        composable("search") {
            val searchViewModel: SearchViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SearchViewModel(repository) as T
                    }
                }
            )
            val uiState by searchViewModel.uiState.collectAsStateWithLifecycle()

            SearchResultsScreen(
                navController = navController,
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isLoading = uiState.isLoading,
                onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
                favoriteRoutineIds = uiState.searchResults.filter { it.isFavorite }.map { it.id }.toSet(),
                onToggleFavorite = { searchViewModel.toggleFavorite(it) }
            )
        }

        composable("help") {
            HelpScreen(navController = navController)
        }
    }
}
