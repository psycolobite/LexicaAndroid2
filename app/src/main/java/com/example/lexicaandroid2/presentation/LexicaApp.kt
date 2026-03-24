package com.example.lexicaandroid2.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.lexicaandroid2.presentation.review.DrivingModeScreen
import com.example.lexicaandroid2.presentation.wordlist.WordListScreen
import com.example.lexicaandroid2.presentation.wordlist.WordListViewModel
import com.example.lexicaandroid2.presentation.wordlist.WordDetailScreen
import com.example.lexicaandroid2.presentation.wordlist.WordDetailViewModel
import com.example.lexicaandroid2.presentation.wordlist.WordDetailViewModelFactory
import com.example.lexicaandroid2.presentation.addwords.AddWordsScreen
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModel
import com.example.lexicaandroid2.presentation.games.MiniGamesScreen
import com.example.lexicaandroid2.presentation.games.matching.MatchingScreen
import com.example.lexicaandroid2.presentation.games.qcm.QcmScreen
import com.example.lexicaandroid2.presentation.games.hangman.HangmanScreen
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.ui.GamificationDemoScreen
import com.example.lexicaandroid2.features.gamification.ui.GamificationViewModel
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.presentation.games.anagrams.AnagramsScreen
import com.example.lexicaandroid2.presentation.games.chrono.ChronoScreen
import com.example.lexicaandroid2.presentation.games.memory.MemoryScreen
import com.example.lexicaandroid2.presentation.games.fillword.FillWordScreen
import com.example.lexicaandroid2.presentation.games.semantic.SemanticScreen
import com.example.lexicaandroid2.presentation.games.spellingadvanced.SpellingAdvancedScreen
import com.example.lexicaandroid2.presentation.games.qcm.SpellingGameScreen
import com.example.lexicaandroid2.presentation.profile.ProfileScreen
import com.example.lexicaandroid2.presentation.profile.ProfileViewModel
import com.example.lexicaandroid2.presentation.profile.ProfileViewModelFactory
import com.example.lexicaandroid2.presentation.dailychallenge.DailyChallengeScreen
import com.example.lexicaandroid2.presentation.dailychallenge.DailyChallengeViewModel
import com.example.lexicaandroid2.presentation.dailychallenge.GameType
import com.example.lexicaandroid2.features.auth.presentation.login.LoginScreen
import com.example.lexicaandroid2.features.auth.presentation.login.LoginViewModel
import com.example.lexicaandroid2.features.auth.presentation.register.RegisterScreen
import com.example.lexicaandroid2.features.auth.presentation.register.RegisterViewModel
import com.example.lexicaandroid2.presentation.admin.AdminConfig
import com.example.lexicaandroid2.presentation.admin.AdminScreen
import com.example.lexicaandroid2.presentation.admin.AdminViewModel
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModel
import com.example.lexicaandroid2.presentation.settings.SettingsScreen
import com.example.lexicaandroid2.presentation.settings.SettingsViewModel
import com.example.lexicaandroid2.presentation.common.LexicaBottomNavBar
import com.example.lexicaandroid2.presentation.navigation.GAME_ROUTES
import com.example.lexicaandroid2.presentation.navigation.Screen
import com.example.lexicaandroid2.presentation.navigation.shouldShowBottomBar
import com.example.lexicaandroid2.presentation.online.OnlineScreen
import com.example.lexicaandroid2.presentation.utilisation.UtilisationScreen
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.features.sync.SyncUiState
import com.example.lexicaandroid2.features.sync.SyncConfirmDialog
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LexicaApp(
    reviewViewModel: ReviewViewModel,
    dashboardViewModel: DashboardViewModel,
    wordListViewModel: WordListViewModel,
    addWordsViewModel: AddWordsViewModel,
    gamificationViewModel: GamificationViewModel,
    miniGamesViewModel: MiniGamesViewModel,
    repository: FlashcardRepository,
    userStatsRepository: UserStatsRepository,
    authRepository: AuthRepository,
    dailyChallengeViewModel: DailyChallengeViewModel,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    adminViewModel: AdminViewModel,
    settingsViewModel: SettingsViewModel,
    syncViewModel: SyncViewModel? = null,
    appVersion: String = "1.0",
    isInitiallyAuthenticated: Boolean = false,
    dailyReviewStatDao: DailyReviewStatDao? = null,
    navController: NavHostController = rememberNavController()
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Auth state pour la navigation conditionnelle
    val currentAuthUser by authRepository.currentUser.collectAsState(initial = null)

    // Review State for Title
    val reviewUiState by reviewViewModel.uiState.collectAsState()

    val topBarTitle = when (currentRoute) {
        Screen.Dashboard.route -> "Lexica"
        Screen.Review.route -> "Review (${reviewUiState.scrum})"
        Screen.WordList.route -> "Mes mots"
        Screen.MiniGames.route -> "Mini-Jeux"
        Screen.MatchingGame.route -> "Correspondance"
        Screen.QcmGame.route -> "QCM"
        Screen.HangmanGame.route -> "Pendu"
        Screen.Gamification.route -> "Progression"
        Screen.SpellingGame.route -> "Dictée"
        Screen.AnagramsGame.route -> "Anagrammes"
        Screen.ChronoGame.route -> "Mode Chrono"
        Screen.MemoryGame.route -> "Memory"
        Screen.FillWordGame.route -> "Définition à Compléter"
        Screen.SemanticGame.route -> "Associations Sémantiques"
        Screen.SpellingAdvancedGame.route -> "Spelling Avancé"
        Screen.Profile.route -> "Mon Profil"
        Screen.DailyChallenge.route -> "Défi du Jour"
        Screen.Login.route -> "Connexion"
        Screen.Register.route -> "Inscription"
        Screen.Admin.route -> "⚙️ Mode Admin"
        Screen.Settings.route -> "Réglages"
        Screen.Utilisation.route -> "Utilisation"
        Screen.Online.route -> "Mode En Ligne"
        Screen.DrivingMode.route -> "Mode voiture"
        else -> if (currentRoute?.startsWith("word/") == true) "Détail du mot" else "Lexica"
    }

    val canNavigateBack = currentRoute == Screen.Review.route || currentRoute == Screen.WordList.route ||
                         currentRoute == Screen.MiniGames.route || currentRoute == Screen.MatchingGame.route ||
                         currentRoute == Screen.QcmGame.route || currentRoute == Screen.HangmanGame.route ||
                         currentRoute == Screen.Gamification.route || currentRoute == Screen.SpellingGame.route ||
                         currentRoute == Screen.AnagramsGame.route || currentRoute == Screen.ChronoGame.route ||
                         currentRoute == Screen.MemoryGame.route || currentRoute == Screen.FillWordGame.route ||
                         currentRoute == Screen.SemanticGame.route || currentRoute == Screen.SpellingAdvancedGame.route ||
                         currentRoute == Screen.Profile.route || currentRoute == Screen.DailyChallenge.route ||
                         currentRoute == Screen.Login.route || currentRoute == Screen.Register.route ||
                         currentRoute == Screen.Admin.route ||
                         currentRoute == Screen.Settings.route ||
                         currentRoute == Screen.Utilisation.route ||
                         currentRoute == Screen.Online.route ||
                         currentRoute == Screen.DrivingMode.route ||
                         currentRoute?.startsWith("word/") == true

    val shouldShowTopBar = currentRoute !in GAME_ROUTES &&
        currentRoute != Screen.Login.route &&
        currentRoute != Screen.Register.route

    val syncUiState by remember(syncViewModel) {
        syncViewModel?.uiState ?: MutableStateFlow(SyncUiState.Idle)
    }.collectAsState()

    val pendingConflict = syncUiState as? SyncUiState.PendingConflict
    if (pendingConflict != null) {
        SyncConfirmDialog(
            conflictState = pendingConflict,
            onKeepLocal = { syncViewModel?.keepLocal(pendingConflict.uid) },
            onReplaceLocal = { syncViewModel?.confirmReplaceWithCloud(pendingConflict.uid, pendingConflict.cloud) }
        )
    }

    Scaffold(
        topBar = {
            if (shouldShowTopBar) {
                LexicaTopAppBar(
                    title = topBarTitle,
                    canNavigateBack = canNavigateBack,
                    navigateUp = { navController.navigateUp() },
                    onProfileClick = if (currentRoute == Screen.Dashboard.route) {
                        {
                            if (currentAuthUser != null) {
                                navController.navigate(Screen.Profile.route)
                            } else {
                                navController.navigate(Screen.Login.route)
                            }
                        }
                    } else if (currentRoute == Screen.Profile.route) {
                        { }
                    } else null,
                    onSettingsClick = if (currentRoute == Screen.Dashboard.route) {
                        { navController.navigate(Screen.Settings.route) }
                    } else null,
                    useBrandTitle = currentRoute == Screen.Dashboard.route
                )
            }
        },
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                LexicaBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isInitiallyAuthenticated) Screen.Dashboard.route else Screen.Login.route,
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
                    onNavigateToDrivingMode = {
                        navController.navigate(Screen.DrivingMode.route)
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
                    },
                    onNavigateToDailyChallenge = {
                        navController.navigate(Screen.DailyChallenge.route)
                    },
                    onNavigateToUsage = {
                        navController.navigate(Screen.Utilisation.route)
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
                route = Screen.DrivingMode.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) {
                DrivingModeScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
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
                    viewModel = wordListViewModel
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
                    viewModel = miniGamesViewModel,
                    onGameSelected = { gameRoute ->
                        navController.navigate(gameRoute)
                    },
                    onBack = {
                        navController.navigateUp()
                    },
                    dailyChallengeUiState = dailyChallengeViewModel.uiState.collectAsState().value,
                    onNavigateToDailyChallenge = {
                        navController.navigate(Screen.DailyChallenge.route)
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
                    onBack = { navController.navigateUp() },
                    onGoHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAwardXp = { amount -> gamificationViewModel.addXp(amount) },
                    onGameCompleted = { score ->
                        dailyChallengeViewModel.tryCompleteFromGame(GameType.MATCHING, score)
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
                    onBack = { navController.navigateUp() },
                    onAwardXp = { amount -> gamificationViewModel.addXp(amount) },
                    onGameCompleted = { score ->
                        dailyChallengeViewModel.tryCompleteFromGame(GameType.QCM, score)
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
                    onBack = { navController.navigateUp() },
                    onAwardXp = { amount -> gamificationViewModel.addXp(amount) },
                    onGameCompleted = { score ->
                        dailyChallengeViewModel.tryCompleteFromGame(GameType.HANGMAN, score)
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
            composable(route = Screen.SpellingGame.route) {
                SpellingGameScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() },
                    onAwardXp = { amount -> gamificationViewModel.addXp(amount) },
                    onGameCompleted = { score ->
                        dailyChallengeViewModel.tryCompleteFromGame(GameType.SPELLING, score)
                    }
                )
            }
            composable(route = Screen.AnagramsGame.route) {
                AnagramsScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.ChronoGame.route) {
                ChronoScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.MemoryGame.route) {
                MemoryScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.FillWordGame.route) {
                FillWordScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.SemanticGame.route) {
                SemanticScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.SpellingAdvancedGame.route) {
                SpellingAdvancedScreen(
                    repository = repository,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ProfileViewModelFactory(
                        userStatsRepository = userStatsRepository,
                        authRepository = authRepository,
                        flashcardRepository = repository,
                        dailyReviewStatDao = dailyReviewStatDao
                    )
                )
                ProfileScreen(
                    flashcardRepository = repository,
                    userStatsRepository = userStatsRepository,
                    authRepository = authRepository,
                    onBack = { navController.navigateUp() },
                    onSignInRequested = { navController.navigate(Screen.Login.route) },
                    syncViewModel = syncViewModel,
                    dailyReviewStatDao = dailyReviewStatDao,
                    viewModel = profileViewModel
                )
            }
            composable(route = Screen.Admin.route) {
                AdminScreen(
                    viewModel = adminViewModel,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.Login.route) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = Screen.Register.route) {
                RegisterScreen(
                    viewModel = registerViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "word/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                val factory = WordDetailViewModelFactory(cardId, repository)
                val detailViewModel: WordDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                WordDetailScreen(
                    cardId = cardId,
                    viewModel = detailViewModel,
                    onBack = { navController.navigateUp() }
                )
            }
            composable(route = Screen.DailyChallenge.route) {
                val dcUiState by dailyChallengeViewModel.uiState.collectAsState()
                DailyChallengeScreen(
                    uiState = dcUiState,
                    onStartChallenge = { gameType ->
                        dailyChallengeViewModel.markChallengeStarted(gameType)
                        when (gameType) {
                            GameType.MATCHING -> navController.navigate(Screen.MatchingGame.route)
                            GameType.QCM -> navController.navigate(Screen.QcmGame.route)
                            GameType.HANGMAN -> navController.navigate(Screen.HangmanGame.route)
                            GameType.SPELLING -> navController.navigate(Screen.SpellingGame.route)
                        }
                    }
                )
            }
            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    appVersion = appVersion,
                    showAdminEntry = AdminConfig.isAdmin(currentAuthUser?.email),
                    onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                )
            }
            composable(route = Screen.Utilisation.route) {
                UtilisationScreen(
                    onNavigateToWordList = { navController.navigate(Screen.WordList.createRoute(null)) },
                    onNavigateToReview = { navController.navigate(Screen.Review.route) }
                )
            }
            composable(route = Screen.Online.route) {
                OnlineScreen()
            }
        }
    }
}
