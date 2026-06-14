package com.example.lexicaandroid2.presentation.search.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.data.corpus.CorpusSource
import com.example.lexicaandroid2.data.corpus.CorpusSources
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.presentation.addwords.AddWordsScreen
import com.example.lexicaandroid2.presentation.addwords.AddWordsViewModel

// Palette de couleurs premium HSL
private val PremiumPurple = Color(0xFF5D3FD3)
private val PremiumPurpleLight = Color(0xFFF3EFFFF)
private val PremiumGold = Color(0xFFFFBF00)
private val PremiumGreen = Color(0xFF00A86B)
private val PremiumGreenLight = Color(0xFFE8F5E9)
private val PremiumGreyBg = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ExploreScreen(
    corpusIndex: CorpusIndex,
    userPreferences: UserPreferences? = null,
    onNavigateToCatalogue: (String?) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAddWords: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(corpusIndex)
    ),
    addWordsViewModel: AddWordsViewModel,
    onEditCard: (Flashcard) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Charger le premier extrait au montage
    LaunchedEffect(Unit) {
        viewModel.loadInitialExtract(userPreferences)
    }

    // Dialogue pour la définition d'un mot dans la bibliothèque
    val selectedLibWord by viewModel.selectedLibraryWord.collectAsState()
    val libWordDef by viewModel.libraryWordDefinition.collectAsState()
    val libWordCat by viewModel.libraryWordCategory.collectAsState()
    val libWordEx by viewModel.libraryWordExemples.collectAsState()
    val libWordSyn by viewModel.libraryWordSynonymes.collectAsState()
    val libWordIsAdded by viewModel.libraryWordIsAdded.collectAsState()

    var editableDef by remember(libWordDef) { mutableStateOf(libWordDef ?: "") }

    if (selectedLibWord != null) {
        Dialog(onDismissRequest = { viewModel.closeLibraryWordDialog() }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 500.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = selectedLibWord!!,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = PremiumPurple
                    )
                    
                    if (!libWordCat.isNullOrBlank()) {
                        Text(
                            text = libWordCat!!,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Définition",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PremiumPurple
                    )

                    OutlinedTextField(
                        value = editableDef,
                        onValueChange = { editableDef = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        textStyle = TextStyle(fontSize = 14.sp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    if (libWordSyn.isNotEmpty()) {
                        Text(
                            text = "Synonymes : ${libWordSyn.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (libWordEx.isNotEmpty()) {
                        Text(
                            text = "Exemples :\n" + libWordEx.joinToString("\n") { "• $it" },
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.closeLibraryWordDialog() }) {
                            Text("Fermer", color = Color.Gray)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))

                        if (libWordIsAdded) {
                            Button(
                                onClick = {
                                    viewModel.deleteLibraryWord()
                                    viewModel.closeLibraryWordDialog()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retirer")
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.saveLibraryWord(editableDef)
                                    viewModel.closeLibraryWordDialog()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter")
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ExploreTopAppBar(
                selectedMode = uiState.activeMode,
                onBack = onBack,
                onInfoClick = {
                    val state = uiState as? ExploreUiState.ExtractDisplayed
                    onNavigateToCatalogue(state?.extract?.sourceTitle)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sélecteur d'onglets premium
            HubTabRow(
                selectedMode = uiState.activeMode,
                onModeSelected = { viewModel.setHubMode(it) }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(PremiumGreyBg)
            ) {
                // Animation de transition entre les modes
                AnimatedContent(
                    targetState = uiState.activeMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                    },
                    label = "modeTransition"
                ) { targetMode ->
                    when (targetMode) {
                        ExploreHubMode.TEXT_EXTRACTS -> {
                            RenderTextExtractsMode(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        ExploreHubMode.VIDEO_EXTRACTS -> {
                            RenderVideoExtractsMode(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        ExploreHubMode.LIBRARY -> {
                            RenderLibraryMode(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        ExploreHubMode.DICTIONARY -> {
                            AddWordsScreen(
                                viewModel = addWordsViewModel,
                                onEditCard = onEditCard
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// RENDER MODES
// =============================================================================

@Composable
private fun RenderTextExtractsMode(
    uiState: ExploreUiState,
    viewModel: ExploreViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is ExploreUiState.Loading -> {
                LoadingState()
            }
            is ExploreUiState.ExtractDisplayed -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ExtractContent(
                            extract = uiState.extract,
                            wordStates = uiState.wordStates,
                            isTransitioning = uiState.isTransitioning,
                            onToggleWord = viewModel::toggleWord,
                            onShowDefinition = viewModel::showDefinition,
                            onNavigateToNext = viewModel::navigateToNext,
                            onNavigateToPrevious = viewModel::navigateToPrevious
                        )
                    }
                    InterestRatingBar(
                        currentRating = uiState.interestRating,
                        onRate = viewModel::rateExtract,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExtractProgressIndicator(
                        currentIndex = uiState.historyIndex,
                        totalCount = uiState.historySize,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            is ExploreUiState.NoExtractAvailable -> {
                NoExtractState(
                    reason = uiState.reason,
                    suggestion = uiState.suggestion,
                    onRetry = viewModel::retry
                )
            }
            is ExploreUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    isRetryable = uiState.isRetryable,
                    onRetry = viewModel::retry
                )
            }
        }
    }
}

@Composable
private fun RenderVideoExtractsMode(
    uiState: ExploreUiState,
    viewModel: ExploreViewModel
) {
    val state = uiState as? ExploreUiState.ExtractDisplayed ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        VideoPlayerMock(
            extract = state.extract,
            isPlaying = state.isVideoPlaying,
            progress = state.videoProgress,
            currentTime = state.currentVideoTime,
            totalTime = state.totalVideoTime,
            onTogglePlay = viewModel::toggleVideoPlay,
            onSeek = viewModel::seekVideo,
            onNextVideo = viewModel::navigateToNextVideo,
            onPrevVideo = viewModel::navigateToPreviousVideo
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Transcription des sous-titres sous le lecteur
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.04f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = PremiumPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sous-titres & Transcription",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PremiumPurple
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    TextExtractContent(
                        extract = state.extract,
                        wordStates = state.wordStates,
                        onToggleWord = viewModel::toggleWord,
                        onShowDefinition = viewModel::showDefinition,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderLibraryMode(
    uiState: ExploreUiState,
    viewModel: ExploreViewModel
) {
    val selectedBook = uiState.selectedBook
    
    if (selectedBook == null) {
        // Choix du livre à lire
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Bibliothèque de Lecture",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PremiumPurple
                )
                Text(
                    text = "Choisissez un ouvrage pour lire et importer du vocabulaire.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            
            items(CorpusSources.ALL) { source ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectBook(source) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.04f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PremiumPurple.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = PremiumPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = source.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = source.author ?: "Auteur inconnu",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            source.year?.let {
                                Text(
                                    text = "Publié en $it",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = PremiumPurple
                        )
                    }
                }
            }
        }
    } else {
        // Liseur e-book interactif
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header du livre
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { viewModel.selectBook(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour bibliothèque")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = selectedBook.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = selectedBook.author ?: "",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PremiumPurpleLight,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Page ${uiState.currentBookPage + 1} / ${uiState.bookPages.size}",
                        color = PremiumPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Corps du texte interactif
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.04f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    val currentPageText = uiState.bookPages.getOrNull(uiState.currentBookPage) ?: "Texte de la page vide."
                    
                    // Rendu de tous les mots interactifs cliquables
                    LibraryPageHighlightedText(
                        text = currentPageText,
                        onWordClick = { word -> viewModel.lookupLibraryWord(word) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pagination du livre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.previousBookPage() },
                    enabled = uiState.currentBookPage > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Page précédente", modifier = Modifier.size(36.dp))
                }

                LinearProgressIndicator(
                    progress = { (uiState.currentBookPage + 1).toFloat() / uiState.bookPages.size.toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PremiumPurple,
                    trackColor = PremiumPurple.copy(alpha = 0.1f),
                )

                IconButton(
                    onClick = { viewModel.nextBookPage() },
                    enabled = uiState.currentBookPage < uiState.bookPages.size - 1
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Page suivante", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

@Composable
fun HubTabRow(
    selectedMode: ExploreHubMode,
    onModeSelected: (ExploreHubMode) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedMode.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PremiumPurple,
        edgePadding = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        ExploreHubMode.entries.forEach { mode ->
            val label = when (mode) {
                ExploreHubMode.TEXT_EXTRACTS -> "Textes"
                ExploreHubMode.VIDEO_EXTRACTS -> "Vidéos"
                ExploreHubMode.LIBRARY -> "Bibliothèque"
                ExploreHubMode.DICTIONARY -> "Dictionnaire"
            }
            val icon = when (mode) {
                ExploreHubMode.TEXT_EXTRACTS -> Icons.Default.Description
                ExploreHubMode.VIDEO_EXTRACTS -> Icons.Default.PlayArrow
                ExploreHubMode.LIBRARY -> Icons.Default.Book
                ExploreHubMode.DICTIONARY -> Icons.Default.Search
            }
            Tab(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp)) },
                text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }
    }
}

@Composable
fun VideoPlayerMock(
    extract: ExtractUiModel,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onNextVideo: () -> Unit,
    onPrevVideo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background du lecteur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF34495E), Color(0xFF1A252F))
                        )
                    )
            )

            // Boutons de navigation rapides & titres
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = extract.sourceTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${extract.sourceAuthor} (${extract.sourceYear ?: "1968"})",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = PremiumPurple
            ) {
                Text(
                    text = "ARCHIVE INA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Bouton Lecture / Pause au centre
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Lecture",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Contrôles de lecture en bas
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = PremiumPurple,
                        activeTrackColor = PremiumPurple,
                        inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currentTime / $totalTime",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onPrevVideo, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Précédent", tint = Color.White)
                        }
                        IconButton(onClick = onNextVideo, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Suivant", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibraryPageHighlightedText(
    text: String,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val words = text.split(" ")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.forEach { word ->
                val cleanWord = word.replace(Regex("[.,;:!?()\"'«»]"), "")
                Text(
                    text = word,
                    fontSize = 17.sp,
                    lineHeight = 26.sp,
                    modifier = Modifier
                        .clickable { onWordClick(cleanWord) }
                        .padding(horizontal = 2.dp),
                    color = Color.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreTopAppBar(
    selectedMode: ExploreHubMode,
    onBack: () -> Unit,
    onInfoClick: () -> Unit
) {
    val titleText = when (selectedMode) {
        ExploreHubMode.TEXT_EXTRACTS -> "Découvrir des Extraits"
        ExploreHubMode.VIDEO_EXTRACTS -> "Conférences & Archives"
        ExploreHubMode.LIBRARY -> "Bibliothèque"
        ExploreHubMode.DICTIONARY -> "Recherche Dictionnaire"
    }

    TopAppBar(
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        actions = {
            if (selectedMode == ExploreHubMode.TEXT_EXTRACTS) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info source"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = PremiumPurple
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recherche d'extraits pertinents...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.04f))
        ) {
            TextExtractContent(
                extract = extract,
                wordStates = wordStates,
                onToggleWord = onToggleWord,
                onShowDefinition = onShowDefinition,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    SwipeNavigationOverlay(
        onSwipeLeft = onNavigateToNext,
        onSwipeRight = onNavigateToPrevious
    )
}

@Composable
private fun SwipeNavigationOverlay(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxSize()
                .clickable(onClick = onSwipeRight)
        )
        Box(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxSize()
                .clickable(onClick = onSwipeLeft)
        )
    }
}

@Composable
fun InterestRatingBar(
    currentRating: Int?,
    onRate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Intérêt de cet extrait :",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.width(12.dp))

            for (i in 1..5) {
                val isSelected = currentRating != null && i <= currentRating
                IconButton(
                    onClick = { onRate(i) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Note $i",
                        tint = if (isSelected) PremiumGold else Color.LightGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtractProgressIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    if (totalCount <= 1) return

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Extrait ${currentIndex + 1} de $totalCount",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}

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

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
            ) {
                Text("Réessayer")
            }
        }
    }
}

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
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPurple)
                ) {
                    Text("Réessayer")
                }
            }
        }
    }
}
