package com.example.lexicaandroid2.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lexicaandroid2.presentation.common.LexicaTopAppBar
import com.example.lexicaandroid2.presentation.dashboard.DashboardScreen
import com.example.lexicaandroid2.presentation.dashboard.DashboardViewModel
import com.example.lexicaandroid2.presentation.review.ReviewScreen
import com.example.lexicaandroid2.presentation.review.ReviewViewModel
import com.example.lexicaandroid2.presentation.wordlist.WordListScreen
import com.example.lexicaandroid2.presentation.wordlist.WordListViewModel
import com.example.lexicaandroid2.presentation.addwords.AddWordsScreen
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModel
import com.example.lexicaandroid2.presentation.games.MiniGamesScreen
import com.example.lexicaandroid2.presentation.games.matching.MatchingScreen
import com.example.lexicaandroid2.presentation.games.qcm.QcmScreen
import com.example.lexicaandroid2.presentation.games.hangman.HangmanScreen
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.ui.GamificationDemoScreen
import com.example.lexicaandroid2.features.gamification.ui.GamificationViewModel

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Review : Screen("review")
    data object WordList : Screen("wordlist?filter={filter}") {
        fun createRoute(filter: String? = null) = "wordlist?filter=${filter ?: ""}"
    }
    data object AddWords : Screen("add_words")
    data object MiniGames : Screen("mini_games")
    data object MatchingGame : Screen("game_matching")
    data object QcmGame : Screen("game_qcm")
    data object HangmanGame : Screen("game_hangman")
    data object Gamification : Screen("gamification")
}

@Composable
fun LexicaApp(
    reviewViewModel: ReviewViewModel,
    dashboardViewModel: DashboardViewModel,
    wordListViewModel: WordListViewModel,
    addWordsViewModel: AddWordsViewModel,
    gamificationViewModel: GamificationViewModel,
    repository: FlashcardRepository,
    navController: NavHostController = rememberNavController()
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Review State for Title
    val reviewUiState by reviewViewModel.uiState.collectAsState()

    val topBarTitle = when (currentRoute) {
        Screen.Dashboard.route -> "" // No title for Dashboard
        Screen.Review.route -> "Review (${reviewUiState.scrum})"
        Screen.WordList.route -> "Mes mots"
        Screen.MiniGames.route -> "Mini-Jeux"
        Screen.MatchingGame.route -> "Correspondance"
        Screen.QcmGame.route -> "QCM"
        Screen.HangmanGame.route -> "Pendu"
        Screen.Gamification.route -> "Progression"
        else -> "Lexica"
    }

    val canNavigateBack = currentRoute == Screen.Review.route || currentRoute == Screen.WordList.route ||
                         currentRoute == Screen.MiniGames.route || currentRoute == Screen.MatchingGame.route ||
                         currentRoute == Screen.QcmGame.route || currentRoute == Screen.HangmanGame.route ||
                         currentRoute == Screen.Gamification.route

    Scaffold(
        topBar = {
            LexicaTopAppBar(
                title = topBarTitle,
                canNavigateBack = canNavigateBack,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Dashboard.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                }
            ) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToReview = {
                        navController.navigate(Screen.Review.route)
                    },
                    onNavigateToWordList = {
                         navController.navigate(Screen.WordList.createRoute(null))
                    },
                    onNavigateToWordListFiltered = { filter ->
                        navController.navigate(Screen.WordList.createRoute(filter))
                    },
                    onNavigateToAddWords = {
                        navController.navigate(Screen.AddWords.route)
                    },
                    onNavigateToMiniGames = {
                        navController.navigate(Screen.MiniGames.route)
                    }
                )
            }
            composable(
                route = Screen.Review.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                ReviewScreen(
                    viewModel = reviewViewModel,
                    navController = navController
                )
            }
            composable(
                route = Screen.WordList.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) { backStackEntry ->
                val filter = backStackEntry.arguments?.getString("filter")

                LaunchedEffect(filter) {
                    wordListViewModel.onFilterSelected(filter.takeIf { !it.isNullOrBlank() })
                }

                WordListScreen(
                    viewModel = wordListViewModel,
                    navController = navController
                )
            }
            composable(
                route = Screen.AddWords.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                AddWordsScreen(
                    viewModel = addWordsViewModel
                )
            }
            composable(
                route = Screen.MiniGames.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                MiniGamesScreen(
                    onGameSelected = { gameRoute ->
                        navController.navigate(gameRoute)
                    },
                    onBack = {
                        navController.navigateUp()
                    }
                )
            }
            composable(
                route = Screen.MatchingGame.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                MatchingScreen(
                    repository = repository,
                    onBack = {
                        navController.navigateUp()
                    }
                )
            }
            composable(
                route = Screen.QcmGame.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                QcmScreen(
                    repository = repository,
                    onBack = {
                        navController.navigateUp()
                    }
                )
            }
            composable(
                route = Screen.HangmanGame.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                HangmanScreen(
                    repository = repository,
                    onBack = {
                        navController.navigateUp()
                    }
                )
            }
            composable(
                route = Screen.Gamification.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                val userStats by gamificationViewModel.userStats.collectAsState()
                GamificationDemoScreen(
                    userStats = userStats,
                    onAddXp = { amount -> gamificationViewModel.addXp(amount) }
                )
            }
        }
    }
}
