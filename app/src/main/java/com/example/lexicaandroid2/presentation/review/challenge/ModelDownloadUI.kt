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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
            _uiState.update { it.copy(isDownloadSuccess = true) }
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

@Composable
fun ModelDownloadDialog(
    viewModel: ModelDownloadViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState = remember { viewModel.uiState }
    val state = uiState.value
    
    LaunchedEffect(Unit) {
        viewModel.downloadModel()
    }
    
    LaunchedEffect(state.isDownloadSuccess) {
        if (state.isDownloadSuccess) {
            onSuccess()
        }
    }
    
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
            // Titre
            Text(
                text = "🧠 Configuration IA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Explication
            Text(
                text = "Téléchargement du modèle de compréhension sémantique (~25 MB) pour activer les défis avancés...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = state.downloadProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            
            // Progress text
            Text(
                text = "${state.downloadProgress}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Error message
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fermer (utiliser mode basique)")
                }
            }
            
            // Success message
            if (state.isDownloadSuccess) {
                Text(
                    text = "✅ Modèle téléchargé avec succès !",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { onSuccess() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuer")
                }
            }
        }
    }
}

@Composable
fun ModelStatusIndicator(isModelReady: Boolean) {
    val (icon, text, color) = if (isModelReady) {
        Triple("🧠", "Mode IA avancé", Color(0xFF4CAF50))
    } else {
        Triple("📊", "Mode basique (Jaccard)", Color(0xFFFFC107))
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
