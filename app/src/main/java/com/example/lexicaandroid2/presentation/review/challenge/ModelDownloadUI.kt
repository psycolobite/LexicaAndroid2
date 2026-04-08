package com.example.lexicaandroid2.presentation.review.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModelDownloadUiState(
    val downloadProgress: Int = 0,
    val isDownloading: Boolean = false,
    val isDownloadSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ModelDownloadViewModel(
    private val modelManager: ModelDownloadManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModelDownloadUiState())
    val uiState: StateFlow<ModelDownloadUiState> = _uiState.asStateFlow()
    
    fun downloadModel() {
        if (modelManager.isModelCached()) {
            _uiState.update { it.copy(downloadProgress = 100, isDownloadSuccess = true) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, errorMessage = null) }
            try {
                val success = modelManager.downloadModelIfNeeded { progress ->
                    _uiState.update { it.copy(downloadProgress = progress) }
                }
                
                if (success) {
                    _uiState.update { it.copy(isDownloading = false, isDownloadSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(
                            isDownloading = false, 
                            errorMessage = "Téléchargement échoué"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isDownloading = false, 
                        errorMessage = "Erreur : ${e.message}"
                    ) 
                }
            }
        }
    }
}

@Suppress("unused")
@Composable
fun ModelDownloadDialog(
    viewModel: ModelDownloadViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.downloadModel()
    }
    
    LaunchedEffect(state.isDownloadSuccess) {
        if (state.isDownloadSuccess) {
            onSuccess()
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "✨ Améliore la correction de tes réponses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "On prépare une amélioration hors ligne d'environ 26 Mo pour mieux reconnaître les réponses formulées avec tes propres mots. Tu peux aussi continuer tout de suite sans attendre.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { state.downloadProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Text(
                    text = "${state.downloadProgress}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                val errorMessage = state.errorMessage
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }

                if (state.isDownloadSuccess) {
                    Text(
                        text = "✅ C'est prêt : l'app comprendra mieux tes réponses libres.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onSuccess,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continuer")
                    }
                } else {
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Plus tard")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
@Composable
fun ModelStatusIndicator(isModelReady: Boolean) {
    val (icon, text, color) = if (isModelReady) {
        Triple("✨", "Réponses libres mieux reconnues", Color(0xFF4CAF50))
    } else {
        Triple("📝", "Correction standard active", Color(0xFFFFC107))
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = "$icon $text",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(12.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
