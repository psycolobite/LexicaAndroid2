package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor
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
    val shouldShowFixedReviewControls =
        !uiState.showSessionCelebration &&
            !uiState.isLoading &&
            current != null &&
            uiState.currentItemType == ReviewCurrentItemType.NORMAL_QUESTION

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

            if (shouldShowFixedReviewControls && current != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReviewHeader(uiState = uiState)

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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        uiState.showSessionCelebration -> SessionCelebrationView(
                            studiedCount = uiState.studiedCount,
                            xpGained = uiState.sessionCompletionXp
                        )

                        uiState.isLoading -> {
                            CircularProgressIndicator()
                        }

                        current == null && uiState.currentItemType != ReviewCurrentItemType.MATCHING -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight * 0.72f),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyReviewState(onReturnToMenu = { navController.popBackStack() })
                            }
                        }

                        else -> {
                            ReviewHeader(uiState = uiState)

                            when (uiState.currentItemType) {
                                ReviewCurrentItemType.NORMAL_QUESTION -> current?.let { card ->
                                    ReviewContextHint(text = promptTitleForNormal(uiState.presentationMode))
                                    NormalQuestionContent(
                                        uiState = uiState,
                                        card = card,
                                        canEditCard = true,
                                        isFavorite = card.favori,
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

                                ReviewCurrentItemType.QCM -> MultipleChoiceEventContent(
                                    uiState = uiState,
                                    card = current,
                                    onSpeakPrompt = { viewModel.speakCurrentFace() },
                                    onSelectChoice = { viewModel.onChoiceSelected(it) },
                                    onSubmit = { viewModel.submitActiveEvent() },
                                    onContinue = { viewModel.continueAfterEventResult() }
                                )

                                ReviewCurrentItemType.EXTRA_SPELLING,
                                ReviewCurrentItemType.CHALLENGE -> TextInputEventContent(
                                    uiState = uiState,
                                    card = current,
                                    onInputChanged = { viewModel.onEventInputChanged(it) },
                                    onSubmit = { viewModel.submitActiveEvent() },
                                    onSkip = { viewModel.skipActiveEvent() },
                                    onContinue = { viewModel.continueAfterEventResult() },
                                    onSpeakWord = { viewModel.speakCurrentWord() },
                                    onSpeakDefinition = { viewModel.speakCurrentDefinition() }
                                )

                                ReviewCurrentItemType.MATCHING -> MatchingEventContent(
                                    uiState = uiState,
                                    onWordSelected = { viewModel.onMatchingWordSelected(it) },
                                    onDefinitionSelected = { viewModel.onMatchingDefinitionSelected(it) },
                                    onSubmit = { viewModel.submitActiveEvent() },
                                    onContinue = { viewModel.continueAfterEventResult() }
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

@Composable
private fun ReviewHeader(
    uiState: ReviewUiState
) {
    val shouldShowHeaderDetails =
        uiState.eventTitle.isNotBlank() ||
            uiState.isSpeaking ||
            !uiState.ttsStatusMessage.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReviewProgressOverview(uiState = uiState)

        if (shouldShowHeaderDetails) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (uiState.eventTitle.isNotBlank()) {
                        Text(
                            text = uiState.eventTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (uiState.isSpeaking) {
                        Text(
                            text = "🔊 Lecture en cours...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.ttsStatusMessage?.takeIf { it.isNotBlank() }?.let { statusMessage ->
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.ttsReady) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun NormalQuestionContent(
    uiState: ReviewUiState,
    card: Flashcard,
    canEditCard: Boolean,
    isFavorite: Boolean,
    minCardHeight: Dp,
    maxCardHeight: Dp,
    onToggleReveal: () -> Unit,
    onSpeakCurrentFace: () -> Unit,
    onSpeakWord: () -> Unit,
    onSpeakDefinition: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val questionInstanceKey = uiState.normalQuestionInstanceKey.ifBlank {
        "${card.id}:${uiState.presentationMode}"
    }
    var showDetails by rememberSaveable(questionInstanceKey) { mutableStateOf(false) }
    var isBackContentUnlocked by rememberSaveable(questionInstanceKey) {
        mutableStateOf(uiState.isAnswerRevealed)
    }
    LaunchedEffect(questionInstanceKey, uiState.isAnswerRevealed) {
        if (uiState.isAnswerRevealed) {
            isBackContentUnlocked = true
        }
    }
    val rotation by animateFloatAsState(
        targetValue = if (uiState.isAnswerRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "flip"
    )
    val isFront = rotation <= 90f
    val density = LocalDensity.current.density
    val cardDisplay = remember(card, uiState.presentationMode) {
        buildReviewCardDisplay(card, uiState.presentationMode)
    }
    val frontTextStyle = rememberAdaptiveTextStyle(
        text = cardDisplay.frontText,
        availableHeight = minCardHeight,
        preferDisplayStyle = cardDisplay.frontText.length < 40,
        serif = cardDisplay.frontUsesSerif
    )
    val answerTextStyle = rememberAdaptiveTextStyle(
        text = cardDisplay.answerText,
        availableHeight = minCardHeight,
        preferDisplayStyle = cardDisplay.answerText.length < 40,
        serif = true
    )
    val supportTextStyle = rememberAdaptiveTextStyle(
        text = cardDisplay.supportingText,
        availableHeight = minCardHeight,
        preferDisplayStyle = false,
        serif = false
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minCardHeight, max = maxCardHeight)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (uiState.isAnswerRevealed) showDetails = false
                        onToggleReveal()
                    }
            ) {
                if (isFront) {
                    ReviewFrontFace(
                        text = cardDisplay.frontText,
                        onSpeak = onSpeakCurrentFace,
                        enabled = uiState.ttsReady,
                        textStyle = frontTextStyle,
                        minHeight = minCardHeight
                    )
                } else if (!isBackContentUnlocked) {
                    ReviewBackFacePlaceholder(minHeight = minCardHeight)
                } else {
                    val speakPrimaryBack = if (uiState.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION ||
                        uiState.presentationMode == ReviewPresentationMode.SPELLING_CHALLENGE
                    ) {
                        onSpeakWord
                    } else {
                        onSpeakDefinition
                    }
                    val speakSecondaryBack = if (uiState.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION ||
                        uiState.presentationMode == ReviewPresentationMode.SPELLING_CHALLENGE
                    ) {
                        onSpeakDefinition
                    } else {
                        onSpeakWord
                    }
                    ReviewBackFace(
                        answerText = cardDisplay.answerText,
                        supportingText = cardDisplay.supportingText,
                        onSpeakAnswer = speakPrimaryBack,
                        onSpeakSupporting = speakSecondaryBack,
                        enabled = uiState.ttsReady,
                        answerTextStyle = answerTextStyle,
                        supportingTextStyle = supportTextStyle,
                        minHeight = minCardHeight,
                        showSupportingFirst = cardDisplay.showSupportingFirst,
                        definitionOnTop = uiState.presentationMode == ReviewPresentationMode.DEFINITION_TO_WORD ||
                            uiState.presentationMode == ReviewPresentationMode.SEMANTIC_CHALLENGE,
                        showDetails = showDetails,
                        onToggleDetails = { showDetails = !showDetails },
                        card = card,
                        canEditCard = canEditCard,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewBackFacePlaceholder(minHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .graphicsLayer { rotationY = 180f }
    )
}

@Composable
private fun MultipleChoiceEventContent(
    uiState: ReviewUiState,
    card: Flashcard?,
    onSpeakPrompt: () -> Unit,
    onSelectChoice: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    EventSurface {
        if (uiState.eventResultMessage != null || uiState.eventResultSuccessful != null) {
            EventResultCard(uiState, onContinue)
            return@EventSurface
        }

        if (card != null) {
            Text(
                text = if (uiState.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION) decodeReviewText(card.recto) else decodeReviewText(card.verso),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = onSpeakPrompt, enabled = uiState.ttsReady) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lire")
            }
        }

        Text(uiState.eventInstruction, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))

        uiState.eventOptions.forEach { option ->
            val selected = option == uiState.selectedChoice
            Button(
                onClick = { onSelectChoice(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else lexicaPanelContainerColor(),
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = decodeReviewText(option), textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = onSubmit, enabled = uiState.selectedChoice != null, modifier = Modifier.fillMaxWidth()) {
            Text(text = "VALIDER LE QCM")
        }
    }
}

@Composable
private fun TextInputEventContent(
    uiState: ReviewUiState,
    card: Flashcard?,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onSpeakWord: () -> Unit,
    onSpeakDefinition: () -> Unit
) {
    EventSurface {
        if (uiState.eventResultMessage != null || uiState.eventResultSuccessful != null) {
            EventResultCard(uiState, onContinue)
            return@EventSurface
        }

        if (card != null) {
            val contextText = when (uiState.currentItemType) {
                ReviewCurrentItemType.EXTRA_SPELLING -> decodeReviewText(card.verso)
                ReviewCurrentItemType.CHALLENGE -> when (uiState.activeChallengeKind) {
                    ReviewSessionChallengeKind.SEMANTIC -> decodeReviewText(card.recto)
                    ReviewSessionChallengeKind.SPELLING, null -> decodeReviewText(card.verso)
                }
                else -> decodeReviewText(card.recto)
            }
            Text(
                text = contextText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onSpeakWord, enabled = uiState.ttsReady) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lire le mot")
                }
                IconButton(onClick = onSpeakDefinition, enabled = uiState.ttsReady) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lire la définition")
                }
            }
        }

        Text(uiState.eventInstruction, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.eventInput,
            onValueChange = onInputChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = uiState.currentItemType == ReviewCurrentItemType.EXTRA_SPELLING || uiState.activeChallengeKind == ReviewSessionChallengeKind.SPELLING,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            placeholder = {
                Text(
                    text = if (uiState.activeChallengeKind == ReviewSessionChallengeKind.SEMANTIC) {
                        "Décris le sens..."
                    } else {
                        "Tape ta réponse..."
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.canSkipCurrentEvent) {
                Button(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = lexicaPanelContainerColor())
                ) {
                    Text(text = "PASSER")
                }
            }
            Button(
                onClick = onSubmit,
                enabled = uiState.eventInput.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "VALIDER")
            }
        }
    }
}

@Composable
private fun MatchingEventContent(
    uiState: ReviewUiState,
    onWordSelected: (String) -> Unit,
    onDefinitionSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    EventSurface {
        if (uiState.eventResultMessage != null || uiState.eventResultSuccessful != null) {
            EventResultCard(uiState, onContinue)
            return@EventSurface
        }

        Text(uiState.eventInstruction, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.eventCards.forEach { card ->
                    val selected = uiState.matchingSelectedWordId == card.id
                    val assignment = uiState.matchingAssignments[card.id]
                    Button(
                        onClick = { onWordSelected(card.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                assignment != null -> MaterialTheme.colorScheme.tertiaryContainer
                                selected -> MaterialTheme.colorScheme.primary
                                else -> lexicaPanelContainerColor()
                            }
                        )
                    ) {
                        Text(text = decodeReviewText(card.recto), textAlign = TextAlign.Center)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.eventOptions.forEach { definition ->
                    val selected = uiState.matchingSelectedDefinition == definition
                    val assigned = uiState.matchingAssignments.values.contains(definition)
                    Button(
                        onClick = { onDefinitionSelected(definition) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                assigned -> MaterialTheme.colorScheme.tertiaryContainer
                                selected -> MaterialTheme.colorScheme.primary
                                else -> lexicaPanelContainerColor()
                            }
                        )
                    ) {
                        Text(text = decodeReviewText(definition), textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSubmit,
            enabled = uiState.matchingAssignments.size == uiState.eventCards.size && uiState.eventCards.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "VALIDER LES ASSOCIATIONS")
        }
    }
}

@Composable
private fun EventSurface(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

@Composable
private fun EventResultCard(uiState: ReviewUiState, onContinue: () -> Unit) {
    val success = uiState.eventResultSuccessful == true
    val background = if (success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (success) Color(0xFF2E7D32) else Color(0xFFB3261E)

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = background) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = uiState.eventResultMessage.orEmpty(),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            uiState.eventResultCorrectAnswer?.takeIf { it.isNotBlank() }?.let { answer ->
                Text(
                    text = "Réponse attendue : ${decodeReviewText(answer)}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(text = "CONTINUER")
            }
        }
    }
}

@Composable
private fun GradeButton(
    modifier: Modifier,
    text: String,
    fontSize: TextUnit,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = Color.White),
        onClick = onClick
    ) {
        Text(
            text = text,
            maxLines = 2,
            textAlign = TextAlign.Center,
            fontSize = fontSize,
            lineHeight = (fontSize.value + 1).sp
        )
    }
}

@Composable
private fun AudioSettingsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Options audio",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(18.dp)
                    .offset(x = (-1).dp, y = 1.dp)
            )
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.TopEnd)
                    .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewFrontFace(
    text: String,
    onSpeak: () -> Unit,
    enabled: Boolean,
    textStyle: TextStyle,
    minHeight: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .verticalScroll(rememberScrollState())
            .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AudioTextLine(text = text, onSpeak = onSpeak, enabled = enabled, textStyle = textStyle, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReviewBackFace(
    answerText: String,
    supportingText: String,
    onSpeakAnswer: () -> Unit,
    onSpeakSupporting: () -> Unit,
    enabled: Boolean,
    answerTextStyle: TextStyle,
    supportingTextStyle: TextStyle,
    minHeight: Dp,
    showSupportingFirst: Boolean,
    definitionOnTop: Boolean,
    showDetails: Boolean,
    onToggleDetails: () -> Unit,
    card: Flashcard,
    canEditCard: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val topText = if (showSupportingFirst) supportingText else answerText
    val topSpeak = if (showSupportingFirst) onSpeakSupporting else onSpeakAnswer
    val topStyle = if (showSupportingFirst) supportingTextStyle else answerTextStyle
    val topFontWeight = if (showSupportingFirst) null else FontWeight.Bold

    val bottomText = if (showSupportingFirst) answerText else supportingText
    val bottomSpeak = if (showSupportingFirst) onSpeakAnswer else onSpeakSupporting
    val bottomStyle = if (showSupportingFirst) answerTextStyle else supportingTextStyle
    val bottomFontWeight = if (showSupportingFirst) FontWeight.Bold else null

    val definitionText = if (definitionOnTop) topText else bottomText
    val definitionShare = when {
        definitionText.length > 240 -> 0.8f
        definitionText.length > 160 -> 0.7f
        definitionText.length > 90 -> 0.6f
        else -> 0.5f
    }
    val topWeight = if (definitionOnTop) definitionShare else 1f - definitionShare
    val bottomWeight = 1f - topWeight
    val actionRowEstimatedHeight = 56.dp
    val contentMinHeight = (minHeight - actionRowEstimatedHeight).coerceAtLeast(160.dp)
    val topSectionMinHeight = contentMinHeight * topWeight
    val bottomSectionMinHeight = contentMinHeight * bottomWeight
    val sectionModifier = if (showDetails) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .heightIn(min = contentMinHeight)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .verticalScroll(rememberScrollState())
            .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 12.dp)
            .graphicsLayer { rotationY = 180f },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = sectionModifier,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = topSectionMinHeight),
                contentAlignment = Alignment.Center
            ) {
                AudioTextLine(
                    text = topText,
                    onSpeak = topSpeak,
                    enabled = enabled,
                    textStyle = topStyle,
                    fontWeight = topFontWeight
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.52f).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = bottomSectionMinHeight),
                contentAlignment = Alignment.Center
            ) {
                AudioTextLine(
                    text = bottomText,
                    onSpeak = bottomSpeak,
                    enabled = enabled,
                    textStyle = bottomStyle,
                    fontWeight = bottomFontWeight
                )
            }
        }

        if (showDetails) {
            ReviewCardDetails(card = card)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onToggleDetails) {
                Text(text = if (showDetails) "Masquer les détails" else "Plus d'infos")
            }

            if (canEditCard) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        if (isFavorite) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Retirer des favoris")
                        } else {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Ajouter aux favoris")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer la carte")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewFixedBottomControls(
    uiState: ReviewUiState,
    showAudioOptions: Boolean,
    onShowAudioOptionsChange: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onToggleAutoSpeakWord: () -> Unit,
    onToggleAutoSpeakDefinition: () -> Unit,
    onReveal: () -> Unit,
    onGrade: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = uiState.canUndo
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Annuler la dernière réponse"
                    )
                }

                Box {
                    AudioSettingsButton(onClick = { onShowAudioOptionsChange(true) })
                    DropdownMenu(
                        expanded = showAudioOptions,
                        onDismissRequest = { onShowAudioOptionsChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lire le mot automatiquement") },
                            trailingIcon = {
                                Checkbox(
                                    checked = uiState.autoSpeakWord,
                                    onCheckedChange = { onToggleAutoSpeakWord() }
                                )
                            },
                            onClick = { onToggleAutoSpeakWord() }
                        )
                        DropdownMenuItem(
                            text = { Text("Lire la définition automatiquement") },
                            trailingIcon = {
                                Checkbox(
                                    checked = uiState.autoSpeakDefinition,
                                    onCheckedChange = { onToggleAutoSpeakDefinition() }
                                )
                            },
                            onClick = { onToggleAutoSpeakDefinition() }
                        )
                    }
                }
            }

            ReviewPrimaryActionBar(
                isAnswerRevealed = uiState.isAnswerRevealed,
                onReveal = onReveal,
                onGrade = onGrade
            )
        }
    }
}

@Composable
private fun ReviewPrimaryActionBar(
    isAnswerRevealed: Boolean,
    onReveal: () -> Unit,
    onGrade: (Int) -> Unit
) {
    if (!isAnswerRevealed) {
        Button(
            onClick = onReveal,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = "VOIR REPONSE")
        }
    } else {
        val buttonTextSize = 11.sp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradeButton(Modifier.weight(1f), "A REVOIR", buttonTextSize, ReviewButtonColors.Error) { onGrade(0) }
            GradeButton(Modifier.weight(1f), "JE L'AI", buttonTextSize, ReviewButtonColors.Ok) { onGrade(4) }
            GradeButton(Modifier.weight(1f), "TROP\nFACILE", buttonTextSize, ReviewButtonColors.Easy) { onGrade(5) }
        }
    }
}

@Composable
private fun ReviewCardDetails(card: Flashcard) {
    val detailsColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
    val firstExample = card.exemples.firstOrNull()?.let(::decodeReviewText)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (card.categorieGrammaticale.isNotBlank()) {
            Text(
                text = "Nature : ${decodeReviewText(card.categorieGrammaticale)}",
                style = MaterialTheme.typography.bodySmall,
                color = detailsColor
            )
        }
        if (card.synonymes.isNotEmpty()) {
            Text(
                text = "Synonymes : ${card.synonymes.joinToString(", ") { decodeReviewText(it) }}",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = detailsColor
            )
        }
        if (!firstExample.isNullOrBlank()) {
            Text(
                text = "Exemple : $firstExample",
                style = MaterialTheme.typography.bodySmall,
                color = detailsColor
            )
        }
    }
}

@Composable
private fun AudioTextLine(
    text: String,
    onSpeak: () -> Unit,
    enabled: Boolean,
    textStyle: TextStyle,
    fontWeight: FontWeight? = null
) {
    val decodedText = decodeReviewText(text)
    val audioInlineContentId = "audio-inline"
    val inlineText = remember(decodedText) {
        buildAnnotatedString {
            append(decodedText)
            append(" ")
            appendInlineContent(audioInlineContentId, "🔊")
        }
    }
    val inlineContent = mapOf(
        audioInlineContentId to InlineTextContent(
            placeholder = Placeholder(
                width = 20.sp,
                height = 20.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = "Lire l'audio" }
                    .clip(CircleShape)
                    .clickable(enabled = enabled, onClick = onSpeak),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = inlineText,
            inlineContent = inlineContent,
            style = textStyle,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun rememberAdaptiveTextStyle(
    text: String,
    availableHeight: Dp,
    preferDisplayStyle: Boolean,
    serif: Boolean
): TextStyle {
    val baseStyle = when {
        text.length > 260 || availableHeight < 300.dp -> MaterialTheme.typography.bodyMedium
        text.length > 160 -> MaterialTheme.typography.bodyLarge
        text.length > 90 -> MaterialTheme.typography.titleMedium
        text.length > 45 -> MaterialTheme.typography.headlineSmall
        preferDisplayStyle -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineSmall
    }

    val compactStyle = baseStyle.copy(
        lineHeight = (baseStyle.fontSize.value * 1.15f).sp
    )

    return if (serif) compactStyle.copy(fontFamily = FontFamily.Serif) else compactStyle
}

private fun promptTitleForNormal(mode: ReviewPresentationMode): String = when (mode) {
    ReviewPresentationMode.WORD_TO_DEFINITION -> "Quelle est la définition du mot ?"
    ReviewPresentationMode.DEFINITION_TO_WORD -> "À quel mot correspond cette définition ?"
    ReviewPresentationMode.SPELLING_CHALLENGE -> "Défi orthographique"
    ReviewPresentationMode.SEMANTIC_CHALLENGE -> "Défi sémantique"
}

private fun buildReviewCardDisplay(card: Flashcard, mode: ReviewPresentationMode): ReviewCardDisplay {
    val word = decodeReviewText(card.recto)
    val definition = decodeReviewText(card.verso)

    return when (mode) {
        ReviewPresentationMode.WORD_TO_DEFINITION -> ReviewCardDisplay(word, word, definition, true, false)
        ReviewPresentationMode.DEFINITION_TO_WORD -> ReviewCardDisplay(definition, definition, word, false, false)
        ReviewPresentationMode.SPELLING_CHALLENGE -> ReviewCardDisplay(word, word, definition, true, false)
        ReviewPresentationMode.SEMANTIC_CHALLENGE -> ReviewCardDisplay(definition, definition, word, false, false)
    }
}

private fun decodeReviewText(value: String): String {
    return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString()
        .replace('\u00A0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}

private data class ReviewCardDisplay(
    val frontText: String,
    val answerText: String,
    val supportingText: String,
    val frontUsesSerif: Boolean,
    val showSupportingFirst: Boolean
)

private object ReviewButtonColors {
    val Error = Color(0xFFB3261E)
    val Ok = Color(0xFF2E7D32)
    val Easy = Color(0xFF1565C0)
}

@Composable
private fun ReviewContextHint(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ReviewProgressOverview(uiState: ReviewUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.sessionProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 280),
        label = "reviewSessionProgress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = lexicaPanelContainerColor()
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun EmptyReviewState(onReturnToMenu: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Aucune carte à réviser", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onReturnToMenu) { Text(text = "Retour au Menu") }
    }
}

@Composable
private fun SessionCelebrationView(
    studiedCount: Int,
    xpGained: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1F8F4C))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CelebrationConfettiOverlay()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Félicitations !",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Session terminée",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
            Text(
                text = "$studiedCount question(s) validée(s)",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "+$xpGained XP",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CelebrationConfettiOverlay() {
    val pieces = remember {
        listOf(
            ConfettiPiece(0.08f, 0.10f, 12.dp, Color(0xFFFFD54F), 12f),
            ConfettiPiece(0.18f, 0.24f, 10.dp, Color(0xFFFF8A65), -18f),
            ConfettiPiece(0.30f, 0.06f, 9.dp, Color(0xFF4DD0E1), 24f),
            ConfettiPiece(0.44f, 0.18f, 8.dp, Color(0xFF81C784), -12f),
            ConfettiPiece(0.58f, 0.09f, 12.dp, Color(0xFFBA68C8), 18f),
            ConfettiPiece(0.72f, 0.20f, 10.dp, Color(0xFFFFD54F), -26f),
            ConfettiPiece(0.85f, 0.11f, 9.dp, Color(0xFF4FC3F7), 14f),
            ConfettiPiece(0.12f, 0.62f, 11.dp, Color(0xFFCE93D8), 22f),
            ConfettiPiece(0.26f, 0.76f, 10.dp, Color(0xFFFFAB91), -20f),
            ConfettiPiece(0.73f, 0.68f, 11.dp, Color(0xFFA5D6A7), 16f),
            ConfettiPiece(0.88f, 0.58f, 8.dp, Color(0xFFFFF176), -10f)
        )
    }
    val transition = rememberInfiniteTransition(label = "celebrationConfetti")
    val fallProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiFall"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        pieces.forEachIndexed { index, piece ->
            val x = size.width * piece.xFraction
            val yBase = size.height * piece.yFraction
            val drift = if (index % 2 == 0) 1f else -1f
            val y = (yBase + size.height * 0.32f * fallProgress + index.toFloat() * 12f) % size.height
            rotate(piece.rotation + (fallProgress * 40f * drift), pivot = androidx.compose.ui.geometry.Offset(x, y)) {
                drawRoundRect(
                    color = piece.color,
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(piece.size.toPx(), piece.size.toPx() * 0.55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val xFraction: Float,
    val yFraction: Float,
    val size: Dp,
    val color: Color,
    val rotation: Float
)

