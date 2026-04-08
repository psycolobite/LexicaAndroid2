package com.example.lexicaandroid2.presentation.review.challenge

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.sqrt

private const val MODEL_SUCCESS_THRESHOLD = 0.62f
private const val MODEL_PARTIAL_THRESHOLD = 0.40f
private const val DEFAULT_MAX_SEQUENCE_LENGTH = 96

data class SemanticModelBundle(
    val modelFile: File,
    val vocabFile: File
)

private data class RemoteSemanticAsset(
    val fileName: String,
    val minBytes: Long,
    val url: String
)

/**
 * Télécharge et cache localement un bundle cohérent pour embeddings on-device :
 * - un modèle TFLite DistilUSE multilingue quantifié
 * - le vocabulaire WordPiece du tokenizer d'origine
 *
 * Le modèle MiniLM pointé auparavant n'existe pas en `model.tflite` dans le dépôt HF ciblé,
 * ce qui rendait l'ancienne pipeline inexécutable en pratique.
 */
class ModelDownloadManager(private val context: Context) {

    private val bundleSpecs = listOf(
        RemoteSemanticAsset(
            fileName = "semantic_distiluse_multilingual_int8.tflite",
            minBytes = 20_000_000,
            url = "https://huggingface.co/xuying7/distiluse-base-multilingual-cased-v2-tflite-version/resolve/main/model_int8.tflite"
        ),
        RemoteSemanticAsset(
            fileName = "semantic_distiluse_multilingual_vocab.txt",
            minBytes = 500_000,
            url = "https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v2/resolve/main/vocab.txt"
        )
    )

    fun isModelCached(): Boolean = bundleSpecs.all { spec ->
        File(context.filesDir, spec.fileName).let { it.exists() && it.length() >= spec.minBytes }
    }

    suspend fun downloadModelIfNeeded(onProgress: (Int) -> Unit = {}): Boolean = withContext(Dispatchers.IO) {
        if (isModelCached()) {
            onProgress(100)
            return@withContext true
        }

        context.filesDir.mkdirs()
        val tempFiles = mutableListOf<File>()

        return@withContext try {
            val connections = bundleSpecs.map { spec -> spec to openConnection(spec.url) }
            val totalBytes = connections.sumOf { (_, connection) -> connection.contentLengthLong.coerceAtLeast(1L) }
            var downloadedBytes = 0L

            connections.forEach { (spec, connection) ->
                val targetFile = File(context.filesDir, spec.fileName)
                val tempFile = File(context.filesDir, "${spec.fileName}.part")
                tempFiles += tempFile

                connection.inputStream.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            onProgress(((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100))
                        }
                    }
                }

                if (tempFile.length() < spec.minBytes) {
                    error("Fichier téléchargé incomplet : ${spec.fileName}")
                }

                if (targetFile.exists()) {
                    targetFile.delete()
                }
                if (!tempFile.renameTo(targetFile)) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }
            }

            onProgress(100)
            isModelCached()
        } catch (e: Exception) {
            e.printStackTrace()
            bundleSpecs.forEach { spec -> File(context.filesDir, spec.fileName).delete() }
            tempFiles.forEach(File::delete)
            false
        }
    }

    fun getModelBundle(): SemanticModelBundle? {
        if (!isModelCached()) return null
        return SemanticModelBundle(
            modelFile = File(context.filesDir, bundleSpecs[0].fileName),
            vocabFile = File(context.filesDir, bundleSpecs[1].fileName)
        )
    }

    fun getModelFile(): File? = getModelBundle()?.modelFile

    fun getVocabFile(): File? = getModelBundle()?.vocabFile

    private fun openConnection(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            connect()
        }
    }
}

internal interface SentenceEmbeddingEngine {
    fun isReady(): Boolean
    fun embed(text: String): FloatArray?
}

internal class DistilUseWordPieceTokenizer private constructor(
    private val tokenToId: Map<String, Int>,
    private val doLowerCase: Boolean
) {

    constructor(vocabLines: List<String>, doLowerCase: Boolean = false) : this(
        tokenToId = vocabLines.mapIndexedNotNull { index, token ->
            token.trimEnd('\r').takeIf { it.isNotEmpty() }?.let { it to index }
        }.toMap(),
        doLowerCase = doLowerCase
    )

    fun encode(text: String, maxSequenceLength: Int = DEFAULT_MAX_SEQUENCE_LENGTH): IntArray {
        val bodyLimit = (maxSequenceLength - 2).coerceAtLeast(1)
        val pieces = basicTokenize(text)
            .flatMap(::wordPieceTokenize)
            .take(bodyLimit)
            .ifEmpty { listOf(UNKNOWN_TOKEN) }

        val tokens = buildList {
            add(CLS_TOKEN)
            addAll(pieces)
            add(SEP_TOKEN)
        }

        return tokens.map { tokenToId[it] ?: unknownTokenId }.toIntArray()
    }

    private fun basicTokenize(text: String): List<String> {
        val cleanedText = cleanText(if (doLowerCase) text.lowercase(Locale.ROOT) else text)
        if (cleanedText.isBlank()) return emptyList()

        val tokens = mutableListOf<String>()
        val currentToken = StringBuilder()

        fun flushCurrentToken() {
            if (currentToken.isNotEmpty()) {
                tokens += currentToken.toString()
                currentToken.setLength(0)
            }
        }

        cleanedText.forEach { char ->
            when {
                char.isWhitespace() -> flushCurrentToken()
                isPunctuation(char) -> {
                    flushCurrentToken()
                    tokens += char.toString()
                }
                else -> currentToken.append(char)
            }
        }
        flushCurrentToken()

        return tokens.filter { it.isNotBlank() }
    }

    private fun wordPieceTokenize(token: String): List<String> {
        if (token.length > MAX_INPUT_CHARS_PER_WORD) {
            return listOf(UNKNOWN_TOKEN)
        }

        var start = 0
        val subTokens = mutableListOf<String>()
        while (start < token.length) {
            var end = token.length
            var currentSubToken: String? = null

            while (start < end) {
                val fragment = token.substring(start, end)
                val candidate = if (start == 0) fragment else "##$fragment"
                if (candidate in tokenToId) {
                    currentSubToken = candidate
                    break
                }
                end -= 1
            }

            if (currentSubToken == null) {
                return listOf(UNKNOWN_TOKEN)
            }

            subTokens += currentSubToken
            start = end
        }

        return subTokens
    }

    private fun cleanText(text: String): String = buildString(text.length) {
        text.forEach { char ->
            when {
                isControl(char) -> Unit
                char.isWhitespace() -> append(' ')
                else -> append(char)
            }
        }
    }

    private fun isControl(char: Char): Boolean {
        if (char == '\t' || char == '\n' || char == '\r') return false
        return when (Character.getType(char)) {
            Character.CONTROL.toInt(), Character.FORMAT.toInt() -> true
            else -> false
        }
    }

    private fun isPunctuation(char: Char): Boolean {
        val code = char.code
        if (code in 33..47 || code in 58..64 || code in 91..96 || code in 123..126) {
            return true
        }

        return when (Character.getType(char)) {
            Character.DASH_PUNCTUATION.toInt(),
            Character.START_PUNCTUATION.toInt(),
            Character.END_PUNCTUATION.toInt(),
            Character.CONNECTOR_PUNCTUATION.toInt(),
            Character.OTHER_PUNCTUATION.toInt(),
            Character.INITIAL_QUOTE_PUNCTUATION.toInt(),
            Character.FINAL_QUOTE_PUNCTUATION.toInt() -> true
            else -> false
        }
    }

    private val unknownTokenId: Int = tokenToId[UNKNOWN_TOKEN] ?: 0

    private companion object {
        private const val MAX_INPUT_CHARS_PER_WORD = 100
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
        private const val UNKNOWN_TOKEN = "[UNK]"
    }
}

internal class TFLiteSentenceEmbeddingEngine(
    private val modelManager: ModelDownloadManager
) : SentenceEmbeddingEngine {

    private val lock = Any()
    private var interpreter: Interpreter? = null
    private var tokenizer: DistilUseWordPieceTokenizer? = null

    init {
        initialize()
    }

    override fun isReady(): Boolean = interpreter != null && tokenizer != null

    override fun embed(text: String): FloatArray? {
        val localInterpreter = interpreter ?: return null
        val localTokenizer = tokenizer ?: return null
        val tokenIds = localTokenizer.encode(text)
        if (tokenIds.size < 2) return null

        return synchronized(lock) {
            try {
                localInterpreter.resizeInput(0, intArrayOf(1, tokenIds.size))
                localInterpreter.allocateTensors()

                val outputShape = localInterpreter.getOutputTensor(0).shape()
                if (outputShape.size != 3 || outputShape[0] != 1 || outputShape[2] <= 0) {
                    return@synchronized null
                }

                val sequenceLength = outputShape[1]
                val hiddenSize = outputShape[2]
                val output = Array(1) { Array(sequenceLength) { FloatArray(hiddenSize) } }
                localInterpreter.run(arrayOf(tokenIds), output)

                val pooled = meanPool(
                    tokenEmbeddings = output[0],
                    tokenCount = tokenIds.size.coerceAtMost(sequenceLength)
                )
                normalize(pooled)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun initialize() {
        val bundle = modelManager.getModelBundle() ?: return
        try {
            tokenizer = DistilUseWordPieceTokenizer(
                vocabLines = bundle.vocabFile.readLines(),
                doLowerCase = false
            )
            interpreter = Interpreter(
                bundle.modelFile,
                Interpreter.Options().apply {
                    setNumThreads(2)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            interpreter = null
            tokenizer = null
        }
    }

    private fun meanPool(tokenEmbeddings: Array<FloatArray>, tokenCount: Int): FloatArray {
        val hiddenSize = tokenEmbeddings.firstOrNull()?.size ?: return FloatArray(0)
        val pooled = FloatArray(hiddenSize)
        val contentStart = if (tokenCount > 2) 1 else 0
        val contentEnd = if (tokenCount > 2) tokenCount - 1 else tokenCount
        val validCount = (contentEnd - contentStart).coerceAtLeast(1)

        for (tokenIndex in contentStart until contentEnd.coerceAtMost(tokenEmbeddings.size)) {
            val embedding = tokenEmbeddings[tokenIndex]
            for (dimension in embedding.indices) {
                pooled[dimension] += embedding[dimension]
            }
        }

        for (dimension in pooled.indices) {
            pooled[dimension] /= validCount.toFloat()
        }
        return pooled
    }

    private fun normalize(vector: FloatArray): FloatArray {
        val norm = sqrt(vector.sumOf { value -> (value * value).toDouble() }).toFloat()
        if (norm <= 0f) return vector
        for (index in vector.indices) {
            vector[index] /= norm
        }
        return vector
    }
}

/**
 * Validateur sémantique hybride :
 * - embeddings on-device si le bundle TFLite est disponible
 * - fallback Jaccard sinon ou en cas d'erreur d'inférence
 */
class TFLiteSemanticValidator internal constructor(
    private val embeddingEngine: SentenceEmbeddingEngine,
    private val fallbackValidator: SemanticValidator = JaccardSemanticValidator()
) : SemanticValidator {

    @Suppress("UNUSED_PARAMETER")
    constructor(context: Context, modelManager: ModelDownloadManager) : this(
        embeddingEngine = TFLiteSentenceEmbeddingEngine(modelManager)
    )

    override fun isModelReady(): Boolean = embeddingEngine.isReady()

    override fun validate(userInput: String, expected: String): ValidationResult {
        if (userInput.isBlank()) {
            return ValidationResult(
                isValid = false,
                keywordScore = 0f,
                semanticScore = 0f,
                foundKeywords = emptyList(),
                missingKeywords = emptyList(),
                xpBonus = 0,
                feedbackMessage = "❌ Aucune réponse fournie"
            )
        }

        if (!isModelReady()) {
            return fallbackValidator.validate(userInput, expected)
        }

        val userEmbedding = embeddingEngine.embed(userInput) ?: return fallbackValidator.validate(userInput, expected)
        val expectedEmbedding = embeddingEngine.embed(expected) ?: return fallbackValidator.validate(userInput, expected)

        val semanticScore = cosineSimilarity(userEmbedding, expectedEmbedding)
        val semanticPercent = (semanticScore.coerceAtLeast(0f) * 100).toInt()

        val (isValid, xpBonus, feedbackMessage) = when {
            semanticScore >= MODEL_SUCCESS_THRESHOLD -> {
                Triple(true, 15, "✅ Bonne définition ! Similarité sémantique : ${semanticPercent}%")
            }

            semanticScore >= MODEL_PARTIAL_THRESHOLD -> {
                Triple(false, 5, "💡 Presque ! Reformule encore un peu ta réponse. Similarité : ${semanticPercent}%")
            }

            else -> {
                Triple(false, 0, "❌ La réponse est trop éloignée du sens attendu. Similarité : ${semanticPercent}%")
            }
        }

        return ValidationResult(
            isValid = isValid,
            keywordScore = 0f,
            semanticScore = semanticScore,
            foundKeywords = emptyList(),
            missingKeywords = emptyList(),
            xpBonus = xpBonus,
            feedbackMessage = feedbackMessage
        )
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size || a.isEmpty()) return 0f

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (index in a.indices) {
            dotProduct += a[index].toDouble() * b[index].toDouble()
            normA += a[index].toDouble() * a[index].toDouble()
            normB += b[index].toDouble() * b[index].toDouble()
        }

        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0.0) (dotProduct / denominator).toFloat() else 0f
    }
}
