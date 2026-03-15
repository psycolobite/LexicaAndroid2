package com.example.lexicaandroid2.presentation.review.challenge

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.sqrt

/**
 * Gestionnaire de téléchargement du modèle TFLite MiniLM
 * 
 * Modèle: paraphrase-multilingual-MiniLM-L12-v2
 * Taille: ~25MB
 * Precision: FP32
 * 
 * Téléchargement conditionnel au premier usage + cache persistent
 */
class ModelDownloadManager(private val context: Context) {
    private val modelFileName = "minilm_multilingual.tflite"
    private val modelFile: File = File(context.filesDir, modelFileName)
    
    // URL du modèle (exemple — à adapter selon source réelle)
    // En production : utiliser Firebase ML ou URL directe Hugging Face
    private val modelUrl = "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/model.tflite"
    
    fun isModelCached(): Boolean = modelFile.exists() && modelFile.length() > 1_000_000
    
    suspend fun downloadModelIfNeeded(onProgress: (Int) -> Unit = {}): Boolean = withContext(Dispatchers.IO) {
        if (isModelCached()) return@withContext true
        
        return@withContext try {
            // Créer dossier s'il n'existe pas
            context.filesDir.mkdirs()
            
            // Télécharger le fichier
            val url = URL(modelUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            
            val contentLength = connection.contentLength
            if (contentLength <= 0) return@withContext false
            
            var downloadedBytes = 0
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            url.openStream().use { inputStream ->
                FileOutputStream(modelFile).use { outputStream ->
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = (downloadedBytes * 100) / contentLength
                        onProgress(progress)
                    }
                }
            }
            
            isModelCached()
        } catch (e: Exception) {
            e.printStackTrace()
            // Nettoyer le fichier partiellement téléchargé
            if (modelFile.exists()) modelFile.delete()
            false
        }
    }
    
    fun getModelFile(): File? = if (isModelCached()) modelFile else null
}

/**
 * Implémentation TFLite de la validation sémantique
 * 
 * Encode une phrase → vecteur dense 384-dim
 * Calcule cosine similarity entre deux vecteurs
 * Score >= 0.65 = match sémantique acceptable
 */
class TFLiteSemanticValidator(
    private val context: Context,
    private val modelManager: ModelDownloadManager
) : SemanticValidator {
    
    private var interpreter: Interpreter? = null
    private var isInitialized = false
    
    init {
        initializeInterpreter()
    }
    
    private fun initializeInterpreter() {
        if (!modelManager.isModelCached()) return
        
        try {
            val modelFile = modelManager.getModelFile() ?: return
            interpreter = Interpreter(modelFile)
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            isInitialized = false
        }
    }
    
    override fun isModelReady(): Boolean = isInitialized && interpreter != null
    
    /**
     * Encode une phrase en vecteur dense 384-dim
     * 
     * Processus simplifié (en prod : utiliser tokenizer complet)
     * Pour V1 : approximation vectorielle
     */
    private fun encodeSimplified(text: String): FloatArray {
        val normalized = KeywordExtractor.tokenize(text)
        val vector = FloatArray(384)
        
        // Méthode simple : hash des tokens dans le vecteur
        // (En prod vrai : utiliser tokenizer BERT + embedding layer)
        normalized.forEachIndexed { idx, token ->
            val hash = token.hashCode().toLong() and 0xFFFFFFFFL
            val position = (hash % 384).toInt()
            vector[position] += 1f / (idx + 1)  // IDF-like weighting
        }
        
        // Normalisation L2
        val norm = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0f) {
            for (i in vector.indices) vector[i] /= norm
        }
        
        return vector
    }
    
    /**
     * Encode une phrase via TFLite (si disponible) ou fallback simplifié
     */
    private fun encode(text: String): FloatArray {
        if (!isModelReady()) {
            return encodeSimplified(text)
        }
        
        return try {
            // Préparation input (simplifié — en prod : tokenizer complet)
            val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 384), org.tensorflow.lite.DataType.FLOAT32)
            val input = encodeSimplified(text)  // Utiliser embedding simplifié en input
            inputBuffer.loadArray(input)
            
            // Run inference
            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 384), org.tensorflow.lite.DataType.FLOAT32)
            interpreter?.run(arrayOf(inputBuffer.buffer), mapOf(0 to outputBuffer.buffer))
            
            outputBuffer.floatArray
        } catch (e: Exception) {
            e.printStackTrace()
            encodeSimplified(text)
        }
    }
    
    /**
     * Cosine similarity entre deux vecteurs [0, 1]
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        
        for (i in a.indices) {
            dotProduct += a[i].toDouble() * b[i].toDouble()
            normA += (a[i].toDouble() * a[i].toDouble())
            normB += (b[i].toDouble() * b[i].toDouble())
        }
        
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0.0) (dotProduct / denominator).toFloat() else 0f
    }
    
    override fun validate(userInput: String, expected: String): ValidationResult {
        if (userInput.isBlank()) {
            return ValidationResult(
                isValid = false,
                keywordScore = 0f,
                semanticScore = 0f,
                foundKeywords = emptyList(),
                missingKeywords = KeywordExtractor.extractKeywords(expected, topN = 5),
                xpBonus = 0,
                feedbackMessage = "❌ Aucune réponse fournie"
            )
        }
        
        // Calcul scores
        val (foundKeywords, missingKeywords) = KeywordExtractor.analyzeKeywords(userInput, expected, topN = 5)
        val allKeywords = foundKeywords + missingKeywords
        val keywordScore = if (allKeywords.isEmpty()) 1f else foundKeywords.size.toFloat() / allKeywords.size.toFloat()
        
        // Score TFLite (si modèle disponible)
        val semanticScore = if (isModelReady()) {
            val userVector = encode(userInput)
            val expectedVector = encode(expected)
            cosineSimilarity(userVector, expectedVector)
        } else {
            -1f  // Indicateur "pas disponible"
        }
        
        // Règle combinée : (TFLite OU Jaccard) si une couche dispo
        val (isValid, xpBonus, feedbackMessage) = when {
            // Avec TFLite : soit sémantique bon, soit keywords bon
            semanticScore >= 0f && (semanticScore >= 0.65f || keywordScore >= 0.6f) -> {
                val msg = "✅ Bonne définition ! Similarité sémantique : ${(semanticScore * 100).toInt()}%"
                Triple(true, 15, msg)
            }
            // Sans TFLite : fallback sur keywords seuls
            semanticScore < 0f && keywordScore >= 0.6f -> {
                val msg = "✅ Bonne définition ! Mots-clés trouvés : ${foundKeywords.joinToString(", ")}"
                Triple(true, 15, msg)
            }
            // Presque (keywords > 30% ou semantic > 0.4)
            (semanticScore >= 0.4f || keywordScore >= 0.3f) -> {
                val msg = "💡 Presque ! Il manquait : ${missingKeywords.joinToString(", ")}"
                Triple(false, 5, msg)
            }
            // Échec
            else -> {
                val missing = if (missingKeywords.isNotEmpty()) {
                    "Mots-clés manquants : ${missingKeywords.joinToString(", ")}"
                } else {
                    "Sens insuffisant"
                }
                Triple(false, 0, "❌ $missing")
            }
        }
        
        return ValidationResult(
            isValid = isValid,
            keywordScore = keywordScore,
            semanticScore = semanticScore,
            foundKeywords = foundKeywords,
            missingKeywords = missingKeywords,
            xpBonus = xpBonus,
            feedbackMessage = feedbackMessage
        )
    }
}
