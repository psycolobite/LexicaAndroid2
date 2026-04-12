package com.example.lexicaandroid2.domain.usecase

import android.content.Context
import android.util.Log
import com.example.lexicaandroid2.core.network.ConnectivityChecker
import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.WordReserveDao
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.remote.DictionaryService
import java.text.Normalizer
import java.util.UUID

/**
 * Recharge automatiquement la table `word_reserve` lorsqu'elle passe sous le seuil de 100 mots.
 *
 * Conditions d'exécution :
 *  - `reserveDao.count() < THRESHOLD`
 *  - Connexion internet active
 *
 * Stratégie :
 *  - Charge la liste de mots candidats [RareWordsCandidates.CANDIDATES], la mélange
 *  - Filtre les mots déjà présents dans la réserve ou dans les flashcards (anti-doublon)
 *  - Interroge le [DictionaryService] mot par mot
 *  - S'arrête dès que `count() >= THRESHOLD`
 *  - Silencieux en cas d'erreur sur un mot (passe au suivant)
 */
class RefillWordReserveUseCase(
    private val reserveDao: WordReserveDao,
    private val flashcardDao: FlashcardDao,
    private val dictionaryService: DictionaryService,
    private val context: Context
) {

    suspend operator fun invoke() {
        val currentCount = reserveDao.count()
        if (currentCount >= THRESHOLD) {
            Log.d(TAG, "Réserve suffisante ($currentCount mots) — pas de rechargement nécessaire.")
            return
        }

        if (!ConnectivityChecker.isConnected(context)) {
            Log.d(TAG, "Pas de connexion — rechargement annulé.")
            return
        }

        val needed = THRESHOLD - currentCount
        Log.d(TAG, "Réserve faible ($currentCount mots). Rechargement de $needed mots en cours…")

        // Construire l'ensemble des mots déjà présents (réserve + collection) pour éviter les doublons
        val existingInReserve = reserveDao.getAllMots().map { normalize(it) }.toHashSet()
        val existingInFlashcards = flashcardDao.getAllMots().map { normalize(it) }.toHashSet()
        val alreadyKnown = existingInReserve + existingInFlashcards

        // Mélanger les candidats et filtrer les doublons
        val candidates = RareWordsCandidates.CANDIDATES
            .shuffled()
            .filter { normalize(it) !in alreadyKnown }

        if (candidates.isEmpty()) {
            Log.w(TAG, "Tous les candidats sont déjà dans la collection ou la réserve.")
            return
        }

        var addedCount = 0

        for (word in candidates) {
            // Revérifier à chaque lot si on a atteint la cible
            if (reserveDao.count() >= THRESHOLD) break

            try {
                val results = dictionaryService.searchWord(word)
                val best = results.firstOrNull { it.definition.isNotBlank() } ?: continue

                val entity = WordReserveEntity(
                    id = UUID.randomUUID().toString(),
                    mot = best.mot.trim(),
                    definition = best.definition.trim(),
                    synonymes = best.synonymes,
                    exemples = best.exemples,
                    categorieGrammaticale = best.categorieGrammaticale,
                    fromApi = true
                )
                reserveDao.insert(entity)
                addedCount++
                Log.d(TAG, "Ajouté : ${entity.mot} ($addedCount/$needed)")

            } catch (e: Exception) {
                Log.w(TAG, "Échec pour « $word » : ${e.message} — passage au mot suivant.")
            }
        }

        Log.d(TAG, "Rechargement terminé. $addedCount mots ajoutés. Total réserve : ${reserveDao.count()}")
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    companion object {
        private const val THRESHOLD = 100
        private const val TAG = "REFILL_RESERVE"
    }
}


