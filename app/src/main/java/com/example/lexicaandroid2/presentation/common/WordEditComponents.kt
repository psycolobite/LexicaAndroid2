package com.example.lexicaandroid2.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

private val EditAccentColor = Color(0xFF6750A4)

@Composable
fun EditWordIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Modifier le mot"
) {
    IconButton(onClick = onClick, modifier = modifier.size(36.dp)) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = contentDescription,
            tint = EditAccentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EditWordCompactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "Modifier"
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = EditAccentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(label)
    }
}

@Composable
fun WordEditorForm(
    word: String,
    definition: String,
    synonymes: String,
    categorie: String,
    registre: String,
    etymologie: String,
    exemples: String,
    notes: String,
    optionalExpanded: Boolean,
    error: String?,
    onWordChanged: (String) -> Unit,
    onDefinitionChanged: (String) -> Unit,
    onSynonymesChanged: (String) -> Unit,
    onCategorieChanged: (String) -> Unit,
    onRegistreChanged: (String) -> Unit,
    onEtymologieChanged: (String) -> Unit,
    onExemplesChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = word,
            onValueChange = onWordChanged,
            label = { Text("Mot *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            isError = error != null && word.isBlank()
        )
        OutlinedTextField(
            value = definition,
            onValueChange = onDefinitionChanged,
            label = { Text("Définition *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 8,
            isError = error != null && definition.isBlank()
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Champs optionnels",
                color = EditAccentColor,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = if (optionalExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = EditAccentColor
            )
        }

        AnimatedVisibility(
            visible = optionalExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = synonymes,
                    onValueChange = onSynonymesChanged,
                    label = { Text("Synonymes") },
                    placeholder = { Text("Sépare-les par des virgules") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = categorie,
                    onValueChange = onCategorieChanged,
                    label = { Text("Catégorie grammaticale") },
                    placeholder = { Text("ex : nom masculin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = registre,
                    onValueChange = onRegistreChanged,
                    label = { Text("Registre") },
                    placeholder = { Text("ex : familier, soutenu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = etymologie,
                    onValueChange = onEtymologieChanged,
                    label = { Text("Étymologie") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = exemples,
                    onValueChange = onExemplesChanged,
                    label = { Text("Exemples") },
                    supportingText = { Text("Un exemple par ligne, ou séparés par des virgules.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChanged,
                    label = { Text("Notes personnelles") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
            }
        }
    }
}
