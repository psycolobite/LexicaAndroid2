package com.example.lexicaandroid2.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showDeleteDialogState = rememberSaveable { mutableStateOf(false) }
    var showAudioOptions by rememberSaveable { mutableStateOf(false) }
    val current = uiState.currentCard
    val isOrthographicEvent = uiState.currentItemType == ReviewCurrentItemType.EXTRA_SPELLING ||
        uiState.currentItemType == ReviewCurrentItemType.CHALLENGE
    val shouldShowFixedReviewControls =
        !uiState.showSessionCelebration &&
            !uiState.isLoading &&
            current != null &&
            uiState.currentItemType == ReviewCurrentItemType.NORMAL_QUESTION
    val shouldShowFixedOrthographicControls =
        !uiState.showSessionCelebration &&
            !uiState.isLoading &&
            current != null &&
            isOrthographicEvent
    val shouldShowMatchingLayout =
        !uiState.showSessionCelebration &&
            !uiState.isLoading &&
            uiState.currentItemType == ReviewCurrentItemType.MATCHING

    LaunchedEffect(Unit) {
        viewModel.loadSession()
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(uiState.sessionCompletionToken) {
        if (uiState.showSessionCelebration && uiState.sessionCompletionToken != 0L) {
            delay(2000)
            viewModel.loadSession()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (shouldShowFixedReviewControls) {
                ReviewFixedBottomControls(
                    uiState = uiState,
                    showAudioOptions = showAudioOptions,
                    onShowAudioOptionsChange = { showAudioOptions = it },
                    onUndo = { viewModel.undoLastAnswer() },
                    onToggleAutoSpeakWord = { viewModel.toggleAutoSpeakWord() },
                    onToggleAutoSpeakDefinition = { viewModel.toggleAutoSpeakDefinition() },
                    onReveal = { viewModel.toggleAnswerReveal() },
                    onGrade = { quality -> viewModel.gradeCard(quality) }
                )
            } else if (shouldShowFixedOrthographicControls) {
                OrthographicFixedBottomControls(
                    uiState = uiState,
                    showAudioOptions = showAudioOptions,
                    onShowAudioOptionsChange = { showAudioOptions = it },
                    onInputChanged = { viewModel.onEventInputChanged(it) },
                    onToggleAutoSpeakWord = { viewModel.toggleAutoSpeakWord() },
                    onToggleAutoSpeakDefinition = { viewModel.toggleAutoSpeakDefinition() },
                    onValidate = { viewModel.submitActiveEvent() },
                    onContinue = { viewModel.continueAfterEventResult() },
                    onBack = { viewModel.skipActiveEvent() }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val minCardHeight = (screenHeight * 0.50f).coerceAtLeast(280.dp)
            val maxCardHeight = (screenHeight * 0.74f).coerceAtLeast(minCardHeight)
            val scrollState = rememberScrollState()

            if (uiState.showSessionCelebration) {
                SessionCelebrationView(
                    studiedCount = uiState.studiedCount,
                    xpGained = uiState.sessionCompletionXp
                )
            } else if (shouldShowFixedReviewControls && current != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReviewHeader(
                        uiState = uiState,
                        showEventTitle = false
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReviewContextHint(text = promptTitleForNormal(uiState.presentationMode))

                            NormalQuestionContent(
                                uiState = uiState,
                                card = current,
                                canEditCard = true,
                                isFavorite = current.favori,
                                minCardHeight = minCardHeight,
                                maxCardHeight = maxCardHeight,
                                onToggleReveal = { viewModel.toggleAnswerReveal() },
                                onSpeakCurrentFace = { viewModel.speakCurrentFace() },
                                onSpeakWord = { viewModel.speakCurrentWord() },
                                onSpeakDefinition = { viewModel.speakCurrentDefinition() },
                                onToggleFavorite = { viewModel.toggleFavorite() },
                                onDelete = { showDeleteDialogState.value = true }
                            )
                        }
                    }
                }
            } else if (shouldShowFixedOrthographicControls && current != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReviewHeader(
                        uiState = uiState,
                        showEventTitle = false
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isExtraSpelling = uiState.currentItemType == ReviewCurrentItemType.EXTRA_SPELLING
                            val isSpellingChallenge = uiState.currentItemType == ReviewCurrentItemType.CHALLENGE &&
                                (uiState.activeChallengeKind == ReviewSessionChallengeKind.SPELLING || uiState.activeChallengeKind == null)
                            val isSemanticChallenge = uiState.currentItemType == ReviewCurrentItemType.CHALLENGE &&
                                uiState.activeChallengeKind == ReviewSessionChallengeKind.SEMANTIC
                            val isUsageChallenge = uiState.currentItemType == ReviewCurrentItemType.CHALLENGE &&
                                uiState.activeChallengeKind == ReviewSessionChallengeKind.USAGE
                            val contextHint = when {
                                isExtraSpelling -> "Question orthographique"
                                isSpellingChallenge -> "Défi orthographique"
                                isSemanticChallenge -> "Défi sémantique"
                                isUsageChallenge -> "Défi utilisation"
                                else -> uiState.eventInstruction
                            }
                            ReviewContextHint(text = contextHint)

                            OrthographicEventContent(
                                uiState = uiState,
                                card = current,
                                minCardHeight = minCardHeight,
                                maxCardHeight = maxCardHeight,
                                onSpeakWord = { viewModel.speakCurrentWord() },
                                onSpeakDefinition = { viewModel.speakCurrentDefinition() },
                                showContextHint = false
                            )
                        }
                    }
                }
            } else if (shouldShowMatchingLayout) {
                // ── Layout dédié Matching : mots sticky + définitions scrollables ──
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReviewHeader(uiState = uiState, showEventTitle = false)
                    ReviewContextHint(text = uiState.eventInstruction)
                    MatchingDragDropContent(
                        modifier     = Modifier.weight(1f),
                        uiState      = uiState,
                        onWordSelected = { viewModel.onMatchingWordSelected(it) },
                        onDrop       = { wordId, def -> viewModel.onMatchingDrop(wordId, def) },
                        onSubmit     = { viewModel.submitActiveEvent() },
                        onContinue   = { viewModel.continueAfterEventResult() }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else if (current == null && uiState.currentItemType != ReviewCurrentItemType.MATCHING) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight * 0.72f),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyReviewState(onReturnToMenu = { navController.popBackStack() })
                        }
                    } else {
                        ReviewHeader(
                            uiState = uiState,
                            showEventTitle = false
                        )

                        if (uiState.currentItemType == ReviewCurrentItemType.QCM) {
                            ReviewContextHint(text = uiState.eventInstruction)
                            MultipleChoiceEventContent(
                                uiState = uiState,
                                card = current,
                                onSpeakPrompt = { viewModel.speakCurrentFace() },
                                onSelectChoice = { viewModel.onChoiceSelected(it) },
                                onSubmit = { viewModel.submitActiveEvent() },
                                onContinue = { viewModel.continueAfterEventResult() }
                            )
                        } else if (
                            uiState.currentItemType == ReviewCurrentItemType.EXTRA_SPELLING ||
                                uiState.currentItemType == ReviewCurrentItemType.CHALLENGE
                        ) {
                            current?.let { card ->
                                OrthographicEventContent(
                                    uiState = uiState,
                                    card = card,
                                    minCardHeight = minCardHeight,
                                    maxCardHeight = maxCardHeight,
                                    onSpeakWord = { viewModel.speakCurrentWord() },
                                    onSpeakDefinition = { viewModel.speakCurrentDefinition() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialogState.value = false },
            title = { Text(text = "Supprimer la carte ?") },
            text = { Text(text = "Cette action est definitive.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialogState.value = false
                        viewModel.deleteCurrentCard()
                    }
                ) { Text(text = "Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogState.value = false }) {
                    Text(text = "Annuler")
                }
            }
        )
    }
}
