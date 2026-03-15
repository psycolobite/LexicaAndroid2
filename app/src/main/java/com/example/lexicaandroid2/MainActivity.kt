package com.example.lexicaandroid2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.lexicaandroid2.data.importer.DataImporter
import com.example.lexicaandroid2.data.local.LexicaDatabase
import com.example.lexicaandroid2.data.repository.FlashcardRepositoryImpl
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.lexicaandroid2.presentation.settings.AppTheme
import com.example.lexicaandroid2.presentation.settings.SettingsViewModel
import com.example.lexicaandroid2.presentation.settings.SettingsViewModelFactory
import com.example.lexicaandroid2.presentation.settings.UserPrefsRepository
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.features.sync.SyncViewModelFactory

import androidx.navigation.compose.rememberNavController
import com.example.lexicaandroid2.presentation.LexicaApp

import com.example.lexicaandroid2.presentation.dashboard.DashboardViewModel
import com.example.lexicaandroid2.presentation.dashboard.DashboardViewModelFactory
import com.example.lexicaandroid2.data.remote.DictionaryServiceImpl
import com.example.lexicaandroid2.presentation.wordlist.WordListViewModel
import com.example.lexicaandroid2.presentation.wordlist.WordListViewModelFactory
import com.example.lexicaandroid2.data.repository.WordReserveRepositoryImpl
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModel
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModelFactory
import com.example.lexicaandroid2.presentation.dailychallenge.DailyChallengeViewModel
import com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository
import com.example.lexicaandroid2.presentation.admin.AdminViewModel
import com.example.lexicaandroid2.presentation.admin.AdminViewModelFactory
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModel
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            LexicaDatabase::class.java,
            "lexica.db"
        )
            .addMigrations(LexicaDatabase.MIGRATION_3_4, LexicaDatabase.MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
        val dao = database.flashcardDao()
        val reserveDao = database.wordReserveDao()
        val userStatsDao = database.userStatsDao()
        val dailyReviewStatDao = database.dailyReviewStatDao()

        val repository = FlashcardRepositoryImpl(dao)
        val dictionaryService = DictionaryServiceImpl()
        val reserveRepository = WordReserveRepositoryImpl(reserveDao, dao, dictionaryService)
        val userStatsRepository = UserStatsRepositoryImpl(userStatsDao)
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
        } catch (e: Exception) { "1.0" }
        val isInitiallyAuthenticated = FirebaseAuth.getInstance().currentUser != null
        val userPrefsRepository = UserPrefsRepository(applicationContext)
        val settingsViewModel = ViewModelProvider(
            this, SettingsViewModelFactory(userPrefsRepository)
        )[SettingsViewModel::class.java]
        val syncViewModel = ViewModelProvider(
            this, SyncViewModelFactory(authRepository, userStatsRepository, userStatsDao, repository)
        )[SyncViewModel::class.java]

        val reviewFactory = ReviewViewModelFactory(
            repository,
            dailyStatDao = dailyReviewStatDao,
            context = applicationContext,
            adminPrefsRepository = adminPrefsRepository
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
                    val importer = DataImporter(applicationContext, dao, reserveDao)
                    Log.d("DATA_IMPORT", "Starting import...")
                    importer.importFromAssets()
                    importer.importReserve()
                    Log.d("DATA_IMPORT", "Import complete")
                } else if (reserveDao.count() == 0) {
                     val importer = DataImporter(applicationContext, dao, reserveDao)
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
                    MaterialTheme(
                        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
                    ) {
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
                            dailyReviewStatDao = dailyReviewStatDao
                        )
                    }
                }
    }
}
