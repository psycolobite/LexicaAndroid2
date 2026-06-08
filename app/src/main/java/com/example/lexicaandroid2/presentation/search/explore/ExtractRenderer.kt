package com.example.lexicaandroid2.presentation.search.explore

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================
// COULEURS DES STATUTS DE MOTS
// =============================================================================

/**
 * Couleurs pour chaque statut de mot, conformément à la spec R1 §4.
 */
object WordColors {
    val suggestedBackground = Color(0xFFFFF8E1)   // Jaune pastel
    val addedBackground = Color(0xFFE8F5E9)       // Vert pastel
    val transitioningBackground = Color(0xFFE3F2FD) // Bleu pastel (transition)
    val normalBackground = Color.Transparent
    val suggestedText = Color(0xFF795548)         // Marron pour lisibilité
    val addedText = Color(0xFF2E7D32)             // Vert foncé
    val normalText = Color(0xFF212121)            // Texte normal
}

// =============================================================================
// EXTRACT RENDERER — COMPOSANT PRINCIPAL
// =============================================================================

/**
 * Affiche le texte d'un extrait avec les mots surlignés selon leur statut.
 *
 * Gère :
 * - Affichage du texte avec AnnotatedString pour les mots cliquables
 * - Couleurs de fond selon WordStatus (SUGGESTED, ADDED, TRANSITIONING, NORMAL)
 * - Tap court sur mot SUGGESTED/ADDED → toggleWord()
 * - Appui long sur tout mot → showDefinition()
 *
 * @param extract L'extrait à afficher
 * @param wordStates Map mot → statut visuel
 * @param onToggleWord Callback pour ajouter/retirer un mot
 * @param onShowDefinition Callback pour afficher la définition
 * @param modifier Modifier Compose
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextExtractContent(
    extract: ExtractUiModel,
    wordStates: Map<String, WordStatus>,
    onToggleWord: (String) -> Unit,
    onShowDefinition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Texte de l'extrait avec mots surlignés
        HighlightedText(
            text = extract.content,
            wordStates = wordStates,
            onToggleWord = onToggleWord,
            onShowDefinition = onShowDefinition,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Informations sur la source
        ExtractSourceInfo(
            sourceTitle = extract.sourceTitle,
            sourceAuthor = extract.sourceAuthor,
            sourceYear = extract.sourceYear,
            difficulty = extract.difficulty,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// =============================================================================
// TEXTE AVEC MOTS SURlIGNÉS
// =============================================================================

/**
 * Affiche un texte en mettant en évidence les mots selon leur [WordStatus].
 *
 * Utilise [AnnotatedString] pour gérer le surlignage et les interactions.
 * Chaque mot est rendu individuellement avec son fond de couleur.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HighlightedText(
    text: String,
    wordStates: Map<String, WordStatus>,
    onToggleWord: (String) -> Unit,
    onShowDefinition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Découper le texte en tokens (mots et séparateurs)
    val tokens = tokenizeText(text)

    // Style de base pour le texte
    val baseStyle = TextStyle(
        fontSize = 18.sp,
        lineHeight = 28.sp,
        color = WordColors.normalText
    )

    // Afficher chaque token
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Utiliser un flow layout pour le texte en ligne
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            var currentLine = AnnotatedString.Builder()

            for (token in tokens) {
                if (token.isWord) {
                    val word = token.text
                    val status = wordStates[word] ?: WordStatus.NORMAL

                    // Construire le span pour ce mot
                    val spanStyle = getSpanStyleForStatus(status)
                    val annotatedWord = buildAnnotatedString {
                        withStyle(spanStyle) { append(word) }
                    }

                    // Mot cliquable
                    HighlightedWord(
                        word = word,
                        annotatedWord = annotatedWord,
                        status = status,
                        onTap = {
                            if (status == WordStatus.SUGGESTED || status == WordStatus.ADDED) {
                                onToggleWord(word)
                            }
                        },
                        onLongPress = {
                            onShowDefinition(word)
                        }
                    )

                    // Espace après le mot
                    Text(
                        text = " ",
                        style = baseStyle
                    )
                } else {
                    // Séparateur (ponctuation, espace, etc.)
                    Text(
                        text = token.text,
                        style = baseStyle
                    )
                }
            }
        }
    }
}

// =============================================================================
// MOT SURlIGNÉ INDIVIDUEL
// =============================================================================

/**
 * Composable pour un mot unique avec gestion du tap et de l'appui long.
 *
 * @param word Le mot à afficher
 * @param annotatedWord Le mot formaté avec AnnotatedString
 * @param status Le statut visuel du mot
 * @param onTap Callback pour le tap court
 * @param onLongPress Callback pour l'appui long
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HighlightedWord(
    word: String,
    annotatedWord: AnnotatedString,
    status: WordStatus,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            WordStatus.SUGGESTED -> WordColors.suggestedBackground
            WordStatus.ADDED -> WordColors.addedBackground
            WordStatus.TRANSITIONING -> WordColors.transitioningBackground
            WordStatus.NORMAL -> WordColors.normalBackground
        },
        animationSpec = tween(durationMillis = 200),
        label = "wordBackground"
    )

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = annotatedWord,
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 28.sp
            )
        )
    }
}

// =============================================================================
// INFORMATIONS SUR LA SOURCE
// =============================================================================

/**
 * Affiche les informations sur la source de l'extrait.
 */
@Composable
fun ExtractSourceInfo(
    sourceTitle: String,
    sourceAuthor: String,
    sourceYear: String?,
    difficulty: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(
            text = "Source : $sourceTitle",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (sourceAuthor.isNotBlank()) {
            Text(
                text = "Auteur : $sourceAuthor",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (sourceYear != null) {
            Text(
                text = "Année : $sourceYear",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Difficulté : $difficulty",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================================
// FONCTIONS UTILITAIRES
// =============================================================================

/**
 * Token de texte : soit un mot, soit un séparateur.
 */
private data class TextToken(
    val text: String,
    val isWord: Boolean
)

/**
 * Découpe un texte en tokens (mots et séparateurs).
 * Les mots sont des séquences de lettres, les séparateurs sont le reste.
 */
private fun tokenizeText(text: String): List<TextToken> {
    val tokens = mutableListOf<TextToken>()
    val regex = Regex("[\\p{L}\\p{N}'-]+|[^\\p{L}\\p{N}'-]+")
    val matches = regex.findAll(text)

    for (match in matches) {
        val value = match.value
        val isWord = Regex("[\\p{L}]+").matches(value)
        tokens.add(TextToken(value, isWord))
    }

    return tokens
}

/**
 * Retourne le [SpanStyle] correspondant au statut d'un mot.
 */
private fun getSpanStyleForStatus(status: WordStatus): SpanStyle = when (status) {
    WordStatus.SUGGESTED -> SpanStyle(
        color = WordColors.suggestedText,
        fontWeight = FontWeight.Medium
    )
    WordStatus.ADDED -> SpanStyle(
        color = WordColors.addedText,
        fontWeight = FontWeight.SemiBold
    )
    WordStatus.TRANSITIONING -> SpanStyle(
        color = WordColors.normalText,
        fontWeight = FontWeight.Normal
    )
    WordStatus.NORMAL -> SpanStyle(
        color = WordColors.normalText,
        fontWeight = FontWeight.Normal
    )
}
