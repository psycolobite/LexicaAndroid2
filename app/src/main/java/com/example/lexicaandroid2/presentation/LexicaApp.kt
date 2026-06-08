package com.example.lexicaandroid2.presentation

import android.view.MotionEvent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.example.lexicaandroid2.presentation.editword.EditWordScreen
import com.example.lexicaandroid2.presentation.editword.EditWordViewModel
import com.example.lexicaandroid2.presentation.editword.EditWordViewModelFactory
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.presentation.games.MiniGamesScreen
import com.example.lexicaandroid2.presentation.games.matching.MatchingScreen
import com.example.lexicaandroid2.presentation.games.qcm.QcmScreen
import com.example.lexicaandroid2.presentation.games.hangman.HangmanScreen
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
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
import com.example.lexicaandroid2.presentation.search.explore.ExploreScreen
import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferencesRepository
import com.example.lexicaandroid2.domain.usecase.ResetProgressUseCase
import com.example.lexicaandroid2.features.sync.SyncManager
import com.example.lexicaandroid2.features.sync.SyncConflictKind
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.features.sync.SyncUiState
import com.example.lexicaandroid2.features.sync.SyncConfirmDialog
import com.example.lexicaandroid2.presentation.review.challenge.ModelDownloadDialog
import com.example.lexicaandroid2.presentation.review.challenge.ModelDownloadManager
import com.example.lexicaandroid2.presentation.review.challenge.ModelDownloadViewModel
import kotlinx.coroutines.flow.MutableStateFlow

private const val EDITED_CARD_RESULT_KEY = "edited_card_id"

@OptIn(ExperimentalComposeUiApi::class)
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
    corpusIndex: CorpusIndex,
    userPreferencesRepository: UserPreferencesRepository,
    syncViewModel: SyncViewModel? = null,
    appVersion: String = "1.0",
    isInitiallyAuthenticated: Boolean = false,
    dailyReviewStatDao: DailyReviewStatDao? = null,
    resetProgressUseCase: ResetProgressUseCase? = null,
    syncManager: SyncManager? = null,
    navController: NavHostController = rememberNavController()
) {
    val userPreferences by userPreferencesRepository.getPreferences().collectAsState(initial = null)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    val wordListUiState by wordListViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showWordListSelectionMenu by remember { mutableStateOf(false) }
    var showWordListBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showWordListBulkResetConfirm by remember { mutableStateOf(false) }

    // Auth state pour la navigation conditionnelle
    val currentAuthUser by authRepository.currentUser.collectAsState(initial = null)

    val topBarTitle = when (currentRoute) {
        Screen.Dashboard.route -> "Lexica"
        Screen.Review.route -> "Apprendre mes mots"
        Screen.WordList.route -> "Mes mots"
        Screen.AddWords.route -> "Ajouter des mots"
        Screen.MiniGames.route -> "Mini-Jeux"
        Screen.MatchingGame.route -> "Correspondance"
        Screen.QcmGame.route -> "QCM"
        Screen.HangmanGame.route -> "Pendu"
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
        Screen.Explore.route -> "Explorer"
        Screen.EditWord().route -> "Modifier mon mot"
        else -> if (currentRoute?.startsWith("word/") == true) "Détail du mot" else "Lexica"
    }

    val wordListSubtitle = if (currentRoute == Screen.WordList.route) {
        ReviewCardAggregateState.fromFilterKey(wordListUiState.selectedFilter)?.label
    } else {
        null
    }

    val canNavigateBack = currentRoute == Screen.Review.route || currentRoute == Screen.WordList.route ||
                         currentRoute == Screen.AddWords.route ||
                         currentRoute == Screen.MiniGames.route || currentRoute == Screen.MatchingGame.route ||
                         currentRoute == Screen.QcmGame.route || currentRoute == Screen.HangmanGame.route ||
                         currentRoute == Screen.SpellingGame.route ||
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
                          currentRoute == Screen.Explore.route ||
                          currentRoute?.startsWith("edit_word/") == true ||
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
            onReplaceLocal = {
                when (pendingConflict.kind) {
                    SyncConflictKind.EMPTY_CLOUD_ACCOUNT -> syncViewModel?.startFreshOnEmptyCloudAccount(pendingConflict.uid)
                    SyncConflictKind.CLOUD_VS_LOCAL -> pendingConflict.cloud?.let {
                        syncViewModel?.confirmReplaceWithCloud(pendingConflict.uid, it)
                    }
                }
            }
        )
    }

    val syncMessage = syncUiState as? SyncUiState.Message
    if (syncMessage != null) {
        AlertDialog(
            onDismissRequest = { syncViewModel?.dismissMessage() },
            title = { Text("Synchronisation") },
            text = { Text(syncMessage.text) },
            confirmButton = {
                Button(onClick = { syncViewModel?.dismissMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    LaunchedEffect(reviewViewModel) {
        reviewViewModel.promptSemanticModelDownloadOnAppLaunch()
    }

    LaunchedEffect(currentRoute, wordListUiState.isSelectionMode) {
        if (currentRoute != Screen.WordList.route || !wordListUiState.isSelectionMode) {
            showWordListSelectionMenu = false
            showWordListBulkDeleteConfirm = false
            showWordListBulkResetConfirm = false
        }
    }

    if (reviewUiState.showSemanticModelDownloadDialog) {
        val modelDownloadViewModel = remember(reviewUiState.showSemanticModelDownloadDialog) {
            ModelDownloadViewModel(ModelDownloadManager(context.applicationContext))
        }
        ModelDownloadDialog(
            viewModel = modelDownloadViewModel,
            onDismiss = { reviewViewModel.dismissSemanticModelDownload() },
            onSuccess = { reviewViewModel.onSemanticModelDownloaded() }
        )
    }

    if (showWordListBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showWordListBulkDeleteConfirm = false },
            title = { Text("Supprimer ${wordListUiState.selectedCount} mot(s) ?") },
            text = { Text("Les cartes sélectionnées seront supprimées définitivement.") },
            confirmButton = {
                Button(
                    onClick = {
                        showWordListBulkDeleteConfirm = false
                        wordListViewModel.deleteSelectedCards()
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { showWordListBulkDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showWordListBulkResetConfirm) {
        AlertDialog(
            onDismissRequest = { showWordListBulkResetConfirm = false },
            title = { Text("Réinitialiser la progression de ${wordListUiState.selectedCount} mot(s) ?") },
            text = { Text("Les cartes sélectionnées repasseront à zéro pour leur progression de révision.") },
            confirmButton = {
                Button(
                    onClick = {
                        showWordListBulkResetConfirm = false
                        wordListViewModel.resetProgressForSelectedCards()
                    }
                ) {
                    Text("Réinitialiser")
                }
            },
            dismissButton = {
                Button(onClick = { showWordListBulkResetConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    focusManager.clearFocus(force = false)
                }
                false
            }
    ) {
        Scaffold(
            topBar = {
                if (shouldShowTopBar) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        LexicaTopAppBar(
                            title = topBarTitle,
                            subtitle = wordListSubtitle,
                            canNavigateBack = canNavigateBack,
                            navigateUp = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth(),
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
                            useBrandTitle = currentRoute == Screen.Dashboard.route,
                            actionsContent = if (currentRoute == Screen.WordList.route && wordListUiState.isSelectionMode) {
                                {
                                    IconButton(onClick = { showWordListSelectionMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Actions de sélection"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showWordListSelectionMenu,
                                        onDismissRequest = { showWordListSelectionMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Ajouter aux favoris") },
                                            onClick = {
                                                showWordListSelectionMenu = false
                                                wordListViewModel.favoriteSelectedCards()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Supprimer") },
                                            onClick = {
                                                showWordListSelectionMenu = false
                                                showWordListBulkDeleteConfirm = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Réinitialiser la progression") },
                                            onClick = {
                                                showWordListSelectionMenu = false
                                                showWordListBulkResetConfirm = true
                                            }
                                        )
                                    }
                                }
                            } else null
                        )
                    }
                }
            },
            bottomBar = {
                if (shouldShowBottomBar(currentRoute)) {
                    LexicaBottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            ) { backStackEntry ->
                val editedCardId = backStackEntry.savedStateHandle.get<String>(EDITED_CARD_RESULT_KEY)
                LaunchedEffect(editedCardId) {
                    if (editedCardId != null) {
                        reviewViewModel.reloadSessionForSettingsChange()
                        backStackEntry.savedStateHandle.remove<String>(EDITED_CARD_RESULT_KEY)
                    }
                }
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

                val editedCardId = backStackEntry.savedStateHandle.get<String>(EDITED_CARD_RESULT_KEY)
                LaunchedEffect(editedCardId) {
                    if (editedCardId != null) {
                        wordListViewModel.loadWords()
                        backStackEntry.savedStateHandle.remove<String>(EDITED_CARD_RESULT_KEY)
                    }
                }

                WordListScreen(
                    viewModel = wordListViewModel,
                    onEditCard = { card -> navController.navigate(Screen.EditWord().createRoute(card.id)) }
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
            ) { backStackEntry ->
                val editedCardId = backStackEntry.savedStateHandle.get<String>(EDITED_CARD_RESULT_KEY)
                LaunchedEffect(editedCardId) {
                    if (editedCardId != null) {
                        addWordsViewModel.refreshAfterCardEdit()
                        backStackEntry.savedStateHandle.remove<String>(EDITED_CARD_RESULT_KEY)
                    }
                }
                AddWordsScreen(
                    viewModel = addWordsViewModel,
                    onEditCard = { card -> navController.navigate(Screen.EditWord().createRoute(card.id)) }
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
                        dailyReviewStatDao = dailyReviewStatDao,
                        resetProgressUseCase = resetProgressUseCase,
                        syncManager = syncManager
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
                    onBack = { navController.navigateUp() },
                    onReviewSettingsChanged = { reviewViewModel.reloadSessionForSettingsChange() }
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
                val editedCardId = backStackEntry.savedStateHandle.get<String>(EDITED_CARD_RESULT_KEY)
                LaunchedEffect(editedCardId) {
                    if (editedCardId != null) {
                        detailViewModel.refreshCard()
                        backStackEntry.savedStateHandle.remove<String>(EDITED_CARD_RESULT_KEY)
                    }
                }
                WordDetailScreen(
                    cardId = cardId,
                    viewModel = detailViewModel,
                    onBack = { navController.navigateUp() },
                    onEditCard = { card -> navController.navigate(Screen.EditWord().createRoute(card.id)) }
                )
            }
            composable(
                route = Screen.EditWord().route,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                val factory = EditWordViewModelFactory(cardId = cardId, repository = repository)
                val editWordViewModel: EditWordViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                EditWordScreen(
                    viewModel = editWordViewModel,
                    onSaved = { updatedCardId ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(EDITED_CARD_RESULT_KEY, updatedCardId)
                        navController.navigateUp()
                    },
                    onCancel = { navController.navigateUp() }
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
                    privacyPolicyUrl = "https://psycolobite.github.io/LexicaAndroid2/",
                    showAdminEntry = AdminConfig.isAdmin(currentAuthUser?.email),
                    onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                    onTrainingSettingsApplied = { reviewViewModel.invalidateSessionForSettingsChange() }
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
            composable(route = Screen.Explore.route) {
                ExploreScreen(
                    corpusIndex = corpusIndex,
                    userPreferences = userPreferences,
                    onNavigateToCatalogue = { /* TODO: TACHE_R7 */ },
                    onNavigateToSearch = { navController.navigate(Screen.AddWords.route) },
                    onBack = { navController.navigateUp() }
                )
            }
            }
        }
    }
}
