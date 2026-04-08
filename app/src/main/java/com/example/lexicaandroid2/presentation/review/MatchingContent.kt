package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────
// État interne du drag en cours
// ─────────────────────────────────────────────────────────────
private data class ActiveDrag(
    val wordId: String,
    val wordText: String,
    val currentPosition: Offset   // coordonnées fenêtre (window)
)

// ─────────────────────────────────────────────────────────────
// Composant principal
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MatchingDragDropContent(
    modifier: Modifier = Modifier,
    uiState: ReviewUiState,
    onWordSelected: (String) -> Unit,
    onDrop: (wordId: String, definition: String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    // Résultat
    if (uiState.eventResultMessage != null || uiState.eventResultSuccessful != null) {
        EventSurface { EventResultCard(uiState, onContinue) }
        return
    }

    // ── État drag ──────────────────────────────────────────────
    var activeDrag by remember { mutableStateOf<ActiveDrag?>(null) }
    val wordWindowPositions  = remember { mutableStateMapOf<String, Rect>() }
    val dropZoneWindowPos    = remember { mutableStateMapOf<String, Rect>() }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { rootCoords = it }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Zone mots (sticky) ────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.eventCards.forEach { card ->
                        val isAssigned      = uiState.matchingAssignments.containsKey(card.id)
                        val isSelected      = uiState.matchingSelectedWordId == card.id
                        val isDraggingThis  = activeDrag?.wordId == card.id
                        val wordText        = decodeReviewText(card.recto)

                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    val tl = coords.localToWindow(Offset.Zero)
                                    wordWindowPositions[card.id] = Rect(
                                        tl.x, tl.y,
                                        tl.x + coords.size.width,
                                        tl.y + coords.size.height
                                    )
                                }
                                .pointerInput(card.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { localOffset ->
                                            val winTL = wordWindowPositions[card.id]
                                                ?.topLeft ?: Offset.Zero
                                            activeDrag = ActiveDrag(
                                                wordId          = card.id,
                                                wordText        = wordText,
                                                currentPosition = winTL + localOffset
                                            )
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            activeDrag = activeDrag?.copy(
                                                currentPosition = activeDrag!!.currentPosition + amount
                                            )
                                        },
                                        onDragEnd = {
                                            val drag = activeDrag
                                            activeDrag = null
                                            drag?.let { d ->
                                                val hit = dropZoneWindowPos.entries
                                                    .firstOrNull { (_, r) -> r.contains(d.currentPosition) }
                                                if (hit != null) onDrop(d.wordId, hit.key)
                                            }
                                        },
                                        onDragCancel = { activeDrag = null }
                                    )
                                }
                                // Tap court = sélection (pour le mode petits écrans)
                                .clickable { onWordSelected(card.id) }
                        ) {
                            MatchingWordChip(
                                text           = wordText,
                                isAssigned     = isAssigned,
                                isSelected     = isSelected,
                                isBeingDragged = isDraggingThis
                            )
                        }
                    }
                }
            }

            // ── Zone définitions (scrollable) ─────────────────
            // Scroll désactivé pendant un drag pour éviter les conflits de geste
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement  = Arrangement.spacedBy(10.dp),
                contentPadding       = PaddingValues(bottom = 16.dp),
                userScrollEnabled    = activeDrag == null
            ) {
                items(uiState.eventOptions) { definition ->
                    val assignedEntry   = uiState.matchingAssignments.entries
                        .firstOrNull { it.value == definition }
                    val assignedWord    = uiState.eventCards
                        .firstOrNull { it.id == assignedEntry?.key }
                        ?.let { decodeReviewText(it.recto) }
                    val isDropSelected  = uiState.matchingSelectedDefinition == definition
                    val isHovered       = activeDrag != null &&
                        dropZoneWindowPos[definition]?.contains(activeDrag!!.currentPosition) == true

                    DefinitionDropCard(
                        definitionText     = decodeReviewText(definition),
                        assignedWordText   = assignedWord,
                        isDropZoneSelected = isDropSelected,
                        isHovered          = isHovered,
                        onDropZoneClick    = {
                            val selWordId = uiState.matchingSelectedWordId
                            when {
                                // Mot sélectionné → on place dans cette zone
                                selWordId != null -> onDrop(selWordId, definition)
                                // Zone déjà occupée, pas de mot sélectionné → on reprend le mot
                                assignedEntry != null -> onWordSelected(assignedEntry.key)
                                // Zone vide, rien de sélectionné → no-op
                            }
                        },
                        onPositioned       = { rect -> dropZoneWindowPos[definition] = rect }
                    )
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick  = onSubmit,
                        enabled  = uiState.matchingAssignments.size == uiState.eventCards.size
                                && uiState.eventCards.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VALIDER LES ASSOCIATIONS")
                    }
                }
            }
        }

        // ── Ghost sous le doigt pendant le drag ───────────────
        activeDrag?.let { drag ->
            val localPos = rootCoords?.windowToLocal(drag.currentPosition) ?: return@let
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (localPos.x - 50.dp.toPx()).roundToInt(),
                            (localPos.y - 20.dp.toPx()).roundToInt()
                        )
                    }
                    .zIndex(10f)
            ) {
                MatchingWordChip(
                    text           = drag.wordText,
                    isAssigned     = false,
                    isSelected     = true,
                    isBeingDragged = true
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Chip de mot (zone sticky + ghost drag)
// ─────────────────────────────────────────────────────────────
@Composable
private fun MatchingWordChip(
    text: String,
    isAssigned: Boolean,
    isSelected: Boolean,
    isBeingDragged: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isBeingDragged) 1.12f else 1f,
        label = "chip_scale"
    )
    Surface(
        shape         = RoundedCornerShape(50),
        color         = when {
            isBeingDragged || isSelected -> MaterialTheme.colorScheme.primary
            isAssigned                   -> MaterialTheme.colorScheme.secondaryContainer
            else                         -> lexicaPanelContainerColor()
        },
        shadowElevation = if (isBeingDragged) 8.dp else 0.dp,
        modifier = Modifier
            .scale(scale)
            .alpha(if (isBeingDragged) 0.85f else if (isAssigned) 0.5f else 1f)
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = when {
                    isBeingDragged || isSelected -> MaterialTheme.colorScheme.onPrimary
                isAssigned                   -> MaterialTheme.colorScheme.onSecondaryContainer
                else                         -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Carte définition + zone de drop
// ─────────────────────────────────────────────────────────────
@Composable
private fun DefinitionDropCard(
    definitionText: String,
    assignedWordText: String?,
    isDropZoneSelected: Boolean,
    isHovered: Boolean,
    onDropZoneClick: () -> Unit,
    onPositioned: (Rect) -> Unit
) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Partie haute : texte de la définition
            Text(
                text      = definitionText,
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Partie basse : zone de drop
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        when {
                            isHovered          -> MaterialTheme.colorScheme.primaryContainer
                            isDropZoneSelected -> MaterialTheme.colorScheme.secondaryContainer
                            assignedWordText != null -> MaterialTheme.colorScheme.secondaryContainer
                            else               -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                    .onGloballyPositioned { coords ->
                        val tl = coords.localToWindow(Offset.Zero)
                        onPositioned(
                            Rect(tl.x, tl.y, tl.x + coords.size.width, tl.y + coords.size.height)
                        )
                    }
                    .clickable(onClick = onDropZoneClick)
            ) {
                when {
                    assignedWordText != null -> Text(
                        text       = assignedWordText,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    isHovered -> Text(
                        text  = "↓ Dépose ici",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    isDropZoneSelected -> Text(
                        text  = "← Sélectionné",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    else -> Text(
                        text  = "···",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

