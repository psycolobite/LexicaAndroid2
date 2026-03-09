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
import com.example.lexicaandroid2.presentation.review.ReviewViewModel
import com.example.lexicaandroid2.presentation.review.ReviewViewModelFactory
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            LexicaDatabase::class.java,
            "lexica.db"
        )
            .addMigrations(LexicaDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
        val dao = database.flashcardDao()
        val reserveDao = database.wordReserveDao()
        val userStatsDao = database.userStatsDao()

        val repository = FlashcardRepositoryImpl(dao)
        val dictionaryService = DictionaryServiceImpl()
        val reserveRepository = WordReserveRepositoryImpl(reserveDao, dao, dictionaryService)
        val userStatsRepository = UserStatsRepositoryImpl(userStatsDao)
        val gamificationViewModel = GamificationViewModel(userStatsRepository)
        val authRepository = FirebaseAuthRepository()

        val reviewFactory = ReviewViewModelFactory(repository)
        val reviewViewModel = ViewModelProvider(this, reviewFactory)[ReviewViewModel::class.java]

        val dashboardFactory = DashboardViewModelFactory(repository)
        val dashboardViewModel = ViewModelProvider(this, dashboardFactory)[DashboardViewModel::class.java]

        val wordListFactory = WordListViewModelFactory(repository, dictionaryService)
        val wordListViewModel = ViewModelProvider(this, wordListFactory)[WordListViewModel::class.java]

        val addWordsFactory = AddWordsViewModelFactory(reserveRepository)
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
                     // Check if reserve needs import even if main db is populated
                     val importer = DataImporter(applicationContext, dao, reserveDao)
                     importer.importReserve()
                }
            } catch (e: Exception) {
                Log.e("DATA_IMPORT", "Import FAILED: $e", e)
            }

            // Switch back to Main thread to set content
            launch(Dispatchers.Main) {
                setContent {
                    MaterialTheme {
                        val navController = rememberNavController()
                        LexicaApp(
                            navController = navController,
                            reviewViewModel = reviewViewModel,
                            dashboardViewModel = dashboardViewModel,
                            wordListViewModel = wordListViewModel,
                            addWordsViewModel = addWordsViewModel,
                            gamificationViewModel = gamificationViewModel,
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}
