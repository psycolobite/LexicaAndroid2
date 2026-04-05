package com.example.lexicaandroid2

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.lexicaandroid2.data.importer.DataImporter
import com.example.lexicaandroid2.data.local.LexicaDatabase
import com.example.lexicaandroid2.data.repository.FlashcardRepositoryImpl
import com.example.lexicaandroid2.data.repository.ReviewSessionSnapshotRepositoryImpl
import com.example.lexicaandroid2.features.gamification.data.UserStatsRepositoryImpl
import com.example.lexicaandroid2.features.gamification.ui.GamificationViewModel
import com.example.lexicaandroid2.features.auth.data.FirebaseAuthRepository
import com.example.lexicaandroid2.features.auth.presentation.login.LoginViewModel
import com.example.lexicaandroid2.features.auth.presentation.login.LoginViewModelFactory
import com.example.lexicaandroid2.features.auth.presentation.register.RegisterViewModel
import com.example.lexicaandroid2.features.auth.presentation.register.RegisterViewModelFactory
import com.example.lexicaandroid2.presentation.review.ReviewViewModel
import com.example.lexicaandroid2.presentation.review.ReviewViewModelFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.lexicaandroid2.presentation.settings.AppTheme
import com.example.lexicaandroid2.presentation.settings.SettingsViewModel
import com.example.lexicaandroid2.presentation.settings.SettingsViewModelFactory
import com.example.lexicaandroid2.presentation.settings.UserPrefsRepository
import com.example.lexicaandroid2.features.sync.SyncManager
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.features.sync.SyncViewModelFactory

import androidx.navigation.compose.rememberNavController
import com.example.lexicaandroid2.core.tts.hasFrenchVoiceSupport
import com.example.lexicaandroid2.presentation.LexicaApp

import com.example.lexicaandroid2.domain.usecase.ResetProgressUseCase
import com.example.lexicaandroid2.presentation.dashboard.DashboardViewModel
import com.example.lexicaandroid2.presentation.dashboard.DashboardViewModelFactory
import com.example.lexicaandroid2.data.remote.DictionaryServiceImpl
import com.example.lexicaandroid2.presentation.wordlist.WordListViewModel
import com.example.lexicaandroid2.presentation.wordlist.WordListViewModelFactory
import com.example.lexicaandroid2.data.repository.WordReserveRepositoryImpl
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModel
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModelFactory
import com.example.lexicaandroid2.presentation.dailychallenge.DailyChallengeViewModel
import com.example.lexicaandroid2.presentation.admin.AdminConfig
import com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository
import com.example.lexicaandroid2.presentation.admin.AdminViewModel
import com.example.lexicaandroid2.presentation.admin.AdminViewModelFactory
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModel
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModelFactory
import androidx.compose.ui.unit.Density
import com.example.lexicaandroid2.presentation.common.FrenchTtsSetupDialog

class MainActivity : ComponentActivity() {
    private var hasAttemptedFrenchTtsInstallThisLaunch = false
    private var hasRecheckedFrenchTtsAfterInstall = false
    private var shouldShowFrenchTtsDialog by mutableStateOf(false)

    private val checkTtsDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val availableVoices = result.data
            ?.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES)
            .orEmpty()
        val hasFrenchVoice =
            result.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS &&
                hasFrenchVoiceSupport(availableVoices)

        if (!hasFrenchVoice) {
            shouldShowFrenchTtsDialog = true
        } else {
            shouldShowFrenchTtsDialog = false
        }
    }

    private val installTtsDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!hasRecheckedFrenchTtsAfterInstall) {
            hasRecheckedFrenchTtsAfterInstall = true
            checkFrenchTtsVoiceAvailability()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        checkFrenchTtsVoiceAvailability()

        val database = Room.databaseBuilder(
            applicationContext,
            LexicaDatabase::class.java,
            "lexica.db"
        )
            .addMigrations(LexicaDatabase.MIGRATION_3_4, LexicaDatabase.MIGRATION_4_5, LexicaDatabase.MIGRATION_5_6, LexicaDatabase.MIGRATION_6_7, LexicaDatabase.MIGRATION_7_8)
            .fallbackToDestructiveMigration()
            .build()
        val dao = database.flashcardDao()
        val reviewQuestionDao = database.reviewQuestionDao()
        val reviewSessionSnapshotDao = database.reviewSessionSnapshotDao()
        val reserveDao = database.wordReserveDao()
        val userStatsDao = database.userStatsDao()
        val dailyReviewStatDao = database.dailyReviewStatDao()

        val repository = FlashcardRepositoryImpl(dao, reviewQuestionDao)
        val reviewSessionSnapshotRepository = ReviewSessionSnapshotRepositoryImpl(reviewSessionSnapshotDao)
        val dictionaryService = DictionaryServiceImpl()
        val reserveRepository = WordReserveRepositoryImpl(reserveDao, dao, reviewQuestionDao, dictionaryService)
        val userStatsRepository = UserStatsRepositoryImpl(userStatsDao)
        val resetProgressUseCase = ResetProgressUseCase(
            flashcardRepository = repository,
            reviewSessionSnapshotDao = reviewSessionSnapshotDao,
            userStatsRepository = userStatsRepository,
            dailyReviewStatDao = dailyReviewStatDao
        )
        val gamificationViewModel = GamificationViewModel(userStatsRepository)
        val dailyChallengeViewModel = DailyChallengeViewModel(userStatsRepository)
        val authRepository = FirebaseAuthRepository()
        val adminPrefsRepository = AdminPrefsRepository(applicationContext)
        val loginViewModel = ViewModelProvider(
            this, LoginViewModelFactory(authRepository)
        )[LoginViewModel::class.java]
        val registerViewModel = ViewModelProvider(
            this, RegisterViewModelFactory(authRepository)
        )[RegisterViewModel::class.java]
        val adminViewModel = ViewModelProvider(
            this, AdminViewModelFactory(applicationContext, userStatsRepository, dailyReviewStatDao)
        )[AdminViewModel::class.java]
        val miniGamesViewModel = ViewModelProvider(
            this, MiniGamesViewModelFactory(userStatsRepository)
        )[MiniGamesViewModel::class.java]

        val appVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (_: Exception) { "1.0" }
        val isInitiallyAuthenticated = FirebaseAuth.getInstance().currentUser != null
        val userPrefsRepository = UserPrefsRepository(applicationContext)
        val settingsViewModel = ViewModelProvider(
            this, SettingsViewModelFactory(userPrefsRepository)
        )[SettingsViewModel::class.java]
        // SyncManager partagé entre SyncViewModel et ProfileViewModel (pour invalidation post-reset)
        val syncManager = SyncManager(
            firestoreSyncRepository = com.example.lexicaandroid2.features.sync.FirestoreSyncRepository(),
            userStatsDao = userStatsDao,
            userStatsRepository = userStatsRepository,
            flashcardRepository = repository
        )
        val syncViewModel = ViewModelProvider(
            this, SyncViewModelFactory(authRepository, syncManager)
        )[SyncViewModel::class.java]

        val reviewFactory = ReviewViewModelFactory(
            repository,
            dailyStatDao = dailyReviewStatDao,
            context = applicationContext,
            userPrefsRepository = userPrefsRepository,
            adminPrefsRepository = adminPrefsRepository,
            reviewSessionSnapshotRepository = reviewSessionSnapshotRepository,
            isAdminUserProvider = {
                AdminConfig.isAdmin(FirebaseAuth.getInstance().currentUser?.email)
            },
            onSessionXpAwarded = { amount -> gamificationViewModel.addXp(amount) }
        )
        val reviewViewModel = ViewModelProvider(this, reviewFactory)[ReviewViewModel::class.java]

        val dashboardFactory = DashboardViewModelFactory(repository)
        val dashboardViewModel = ViewModelProvider(this, dashboardFactory)[DashboardViewModel::class.java]

        val wordListFactory = WordListViewModelFactory(repository, dictionaryService)
        val wordListViewModel = ViewModelProvider(this, wordListFactory)[WordListViewModel::class.java]

        val addWordsFactory = AddWordsViewModelFactory(
            wordReserveRepository = reserveRepository,
            flashcardRepository = repository
        )
        val addWordsViewModel = ViewModelProvider(this, addWordsFactory)[AddWordsViewModel::class.java]

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (dao.count() == 0) {
                    val importer = DataImporter(applicationContext, dao, reviewQuestionDao, reserveDao)
                    Log.d("DATA_IMPORT", "Starting import...")
                    importer.importFromAssets()
                    importer.importReserve()
                    Log.d("DATA_IMPORT", "Import complete")
                } else if (reserveDao.count() == 0) {
                     val importer = DataImporter(applicationContext, dao, reviewQuestionDao, reserveDao)
                     importer.importReserve()
                }
            } catch (e: Exception) {
                Log.e("DATA_IMPORT", "Import FAILED: $e", e)
            }
        }

        setContent {
                    val settingsUiState by settingsViewModel.uiState.collectAsState()
                    val darkTheme = when (settingsUiState.theme) {
                        AppTheme.LIGHT -> false
                        AppTheme.DARK -> true
                        AppTheme.SYSTEM -> isSystemInDarkTheme()
                    }
                    val density = LocalDensity.current
                    val fontScaleMultiplier =
                        (settingsUiState.fontSize / UserPrefsRepository.DEFAULT_FONT_SIZE).coerceIn(0.75f, 1.375f)
                    CompositionLocalProvider(
                        LocalDensity provides Density(
                            density = density.density,
                            fontScale = density.fontScale * fontScaleMultiplier
                        )
                    ) {
                        MaterialTheme(
                            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
                        ) {
                            val systemBarColor = MaterialTheme.colorScheme.surface.toArgb()
                            SideEffect {
                                window.statusBarColor = systemBarColor
                                window.navigationBarColor = systemBarColor
                                WindowCompat.getInsetsController(window, window.decorView).apply {
                                    isAppearanceLightStatusBars = !darkTheme
                                    isAppearanceLightNavigationBars = !darkTheme
                                }
                            }
                            val navController = rememberNavController()
                            LexicaApp(
                                navController = navController,
                                reviewViewModel = reviewViewModel,
                                dashboardViewModel = dashboardViewModel,
                                wordListViewModel = wordListViewModel,
                                addWordsViewModel = addWordsViewModel,
                                gamificationViewModel = gamificationViewModel,
                                miniGamesViewModel = miniGamesViewModel,
                                repository = repository,
                                userStatsRepository = userStatsRepository,
                                authRepository = authRepository,
                                dailyChallengeViewModel = dailyChallengeViewModel,
                                loginViewModel = loginViewModel,
                                registerViewModel = registerViewModel,
                                adminViewModel = adminViewModel,
                                settingsViewModel = settingsViewModel,
                                syncViewModel = syncViewModel,
                                appVersion = appVersion,
                                isInitiallyAuthenticated = isInitiallyAuthenticated,
                                dailyReviewStatDao = dailyReviewStatDao,
                                resetProgressUseCase = resetProgressUseCase,
                                syncManager = syncManager
                            )

                            if (shouldShowFrenchTtsDialog) {
                                FrenchTtsSetupDialog(
                                    onDismiss = { shouldShowFrenchTtsDialog = false },
                                    onOpenTtsSetup = {
                                        shouldShowFrenchTtsDialog = false
                                        launchFrenchTtsInstallFlow()
                                    }
                                )
                            }
                        }
                    }
                }
    }

    private fun checkFrenchTtsVoiceAvailability() {
        val checkIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        if (checkIntent.resolveActivity(packageManager) != null) {
            checkTtsDataLauncher.launch(checkIntent)
        } else {
            shouldShowFrenchTtsDialog = true
        }
    }

    private fun launchFrenchTtsInstallFlow() {
        if (hasAttemptedFrenchTtsInstallThisLaunch) {
            openTtsSettingsFallback()
            return
        }

        hasAttemptedFrenchTtsInstallThisLaunch = true

        val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        if (installIntent.resolveActivity(packageManager) != null) {
            installTtsDataLauncher.launch(installIntent)
        } else {
            openTtsSettingsFallback()
        }
    }

    private fun openTtsSettingsFallback() {
        val settingsIntent = Intent("com.android.settings.TTS_SETTINGS")
        if (settingsIntent.resolveActivity(packageManager) != null) {
            startActivity(settingsIntent)
        } else {
            Toast.makeText(
                this,
                "Aucun écran système TTS disponible pour installer une voix française.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
