package com.example.lexicaandroid2.presentation.search.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import com.example.lexicaandroid2.data.corpus.CorpusIndex

// =============================================================================
// EXPLORE SCREEN — ÉCRAN PRINCIPAL
// =============================================================================

/**
 * Écran principal d'exploration des extraits.
 *
 * Structure (cf. ExploreScreenSpec.kt §1) :
 * - TopAppBar avec titre "Explorer" et icône info source
 * - Zone extrait principale (weight 1f) avec TextExtractContent
 * - Barre de note d'intérêt (1-5) repliable en bas de l'extrait
 * - Barre basse secondaire avec boutons "Chercher des ouvrages" et "Rechercher des mots"
 *
 * @param viewModel ViewModel de l'écran
 * @param userPreferences Préférences utilisateur pour la personnalisation
 * @param onNavigateToCatalogue Callback vers le catalogue d'ouvrages
 * @param onNavigateToSearch Callback vers la recherche de mots existante
 * @param onBack Callback pour revenir en arrière
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    corpusIndex: CorpusIndex,
    userPreferences: UserPreferences? = null,
    onNavigateToCatalogue: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(corpusIndex)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Charger le premier extrait au montage
    LaunchedEffect(Unit) {
        viewModel.loadInitialExtract(userPreferences)
    }

    Scaffold(
        topBar = {
            ExploreTopAppBar(
                onBack = onBack,
                onInfoClick = {
                    // Afficher les infos de la source (stub)
                }
            )
        },
        bottomBar = {
            ExploreBottomBar(
                onNavigateToCatalogue = onNavigateToCatalogue,
                onNavigateToSearch = onNavigateToSearch
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ExploreUiState.Loading -> {
                    LoadingState()
                }

                is ExploreUiState.ExtractDisplayed -> {
                    ExtractDisplayedState(
                        state = state,
                        onToggleWord = viewModel::toggleWord,
                        onShowDefinition = viewModel::showDefinition,
                        onRateExtract = viewModel::rateExtract,
                        onNavigateToNext = viewModel::navigateToNext,
                        onNavigateToPrevious = viewModel::navigateToPrevious
                    )
                }

                is ExploreUiState.NoExtractAvailable -> {
                    NoExtractState(
                        reason = state.reason,
                        suggestion = state.suggestion,
                        onRetry = viewModel::retry
                    )
                }

                is ExploreUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        isRetryable = state.isRetryable,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

// =============================================================================
// TOP APP BAR
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreTopAppBar(
    onBack: () -> Unit,
    onInfoClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Explorer",
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info source"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// =============================================================================
// ÉTAT : CHARGEMENT
// =============================================================================

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recherche du meilleur extrait...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// ÉTAT : EXTRAIT AFFICHÉ
// =============================================================================

@Composable
private fun ExtractDisplayedState(
    state: ExploreUiState.ExtractDisplayed,
    onToggleWord: (String) -> Unit,
    onShowDefinition: (String) -> Unit,
    onRateExtract: (Int) -> Unit,
    onNavigateToNext: () -> Unit,
    onNavigateToPrevious: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Zone extrait principale — prend tout l'espace disponible
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ExtractContent(
                extract = state.extract,
                wordStates = state.wordStates,
                isTransitioning = state.isTransitioning,
                onToggleWord = onToggleWord,
                onShowDefinition = onShowDefinition,
                onNavigateToNext = onNavigateToNext,
                onNavigateToPrevious = onNavigateToPrevious
            )
        }

        // Barre de note d'intérêt
        InterestRatingBar(
            currentRating = state.interestRating,
            onRate = onRateExtract,
            modifier = Modifier.fillMaxWidth()
        )

        // Indicateur de progression
        ExtractProgressIndicator(
            currentIndex = state.historyIndex,
            totalCount = state.historySize,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// =============================================================================
// CONTENU DE L'EXTRAIT AVEC SWIPE NAVIGATION
// =============================================================================

@Composable
private fun ExtractContent(
    extract: ExtractUiModel,
    wordStates: Map<String, WordStatus>,
    isTransitioning: Boolean,
    onToggleWord: (String) -> Unit,
    onShowDefinition: (String) -> Unit,
    onNavigateToNext: () -> Unit,
    onNavigateToPrevious: () -> Unit
) {
    // Animation de transition
    AnimatedVisibility(
        visible = !isTransitioning,
        enter = slideInHorizontally(
            animationSpec = tween(300),
            initialOffsetX = { fullWidth -> fullWidth }
        ),
        exit = slideOutHorizontally(
            animationSpec = tween(300),
            targetOffsetX = { fullWidth -> -fullWidth }
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            // Contenu texte de l'extrait
            TextExtractContent(
                extract = extract,
                wordStates = wordStates,
                onToggleWord = onToggleWord,
                onShowDefinition = onShowDefinition,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Zones de swipe (bordures gauche/droite)
    SwipeNavigationOverlay(
        onSwipeLeft = onNavigateToNext,
        onSwipeRight = onNavigateToPrevious
    )
}

// =============================================================================
// OVERLAY DE SWIPE NAVIGATION
// =============================================================================

/**
 * Overlay transparent qui capture les gestes de swipe sur les bordures.
 * La zone centrale (texte) reste libre pour les taps sur les mots.
 */
@Composable
private fun SwipeNavigationOverlay(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    // Note : Dans une implémentation réelle, on utiliserait
    // Modifier.pointerInput ou HorizontalPager.
    // Pour la V1, on utilise des zones cliquables sur les bordures.
    Row(modifier = Modifier.fillMaxSize()) {
        // Zone de swipe gauche (précédent)
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxSize()
                .clickable(onClick = onSwipeRight)
        )

        // Zone centrale (texte) — laisse passer les interactions
        Box(modifier = Modifier.weight(1f))

        // Zone de swipe droite (suivant)
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxSize()
                .clickable(onClick = onSwipeLeft)
        )
    }
}

// =============================================================================
// BARRE DE NOTE D'INTÉRÊT
// =============================================================================

/**
 * Barre de note d'intérêt (1-5) pour l'extrait courant.
 * Conforme à la spec R1 §6.2 : barre légère, repliable.
 */
@Composable
fun InterestRatingBar(
    currentRating: Int?,
    onRate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Intérêt :",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            for (i in 1..5) {
                val isSelected = currentRating != null && i <= currentRating
                IconButton(
                    onClick = { onRate(i) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Note $i",
                        tint = if (isSelected)
                            Color(0xFFFFB800)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// INDICATEUR DE PROGRESSION
// =============================================================================

/**
 * Indicateur de progression (dots) montrant la position dans la session.
 */
@Composable
private fun ExtractProgressIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    if (totalCount <= 1) return

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${currentIndex + 1} / $totalCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================================
// BARRE BASSE SECONDAIRE
// =============================================================================

/**
 * Barre basse avec les deux actions secondaires :
 * - "Chercher des ouvrages" → catalogue
 * - "Rechercher des mots" → recherche existante
 */
@Composable
fun ExploreBottomBar(
    onNavigateToCatalogue: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onNavigateToCatalogue,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Chercher des ouvrages",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onNavigateToSearch,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Rechercher des mots",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

// =============================================================================
// ÉTAT : AUCUN EXTRAIT DISPONIBLE
// =============================================================================

@Composable
private fun NoExtractState(
    reason: NoExtractReason,
    suggestion: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = when (reason) {
                    NoExtractReason.CORPUS_EMPTY -> "Aucun corpus disponible"
                    NoExtractReason.PROFILE_TOO_NARROW -> "Profil trop spécifique"
                    NoExtractReason.ALL_CONSUMED -> "Plus d'extraits disponibles"
                    NoExtractReason.PENDING_INDEX -> "Indexation en cours"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            if (suggestion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRetry) {
                Text("Réessayer")
            }
        }
    }
}

// =============================================================================
// ÉTAT : ERREUR
// =============================================================================

@Composable
private fun ErrorState(
    message: String,
    isRetryable: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Une erreur est survenue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            if (isRetryable) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRetry) {
                    Text("Réessayer")
                }
            }
        }
    }
}
