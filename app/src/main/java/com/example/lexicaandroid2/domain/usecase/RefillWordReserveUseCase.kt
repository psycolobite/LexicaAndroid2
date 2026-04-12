package com.example.lexicaandroid2.domain.usecase

import android.content.Context
import android.util.Log
import com.example.lexicaandroid2.core.network.ConnectivityChecker
import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.WordReserveDao
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.data.remote.WiktionnaireCategorySource
import java.text.Normalizer
import java.util.UUID

/**
 * Recharge automatiquement la table `word_reserve` lorsqu'elle passe sous [THRESHOLD] mots.
 *
 * Conditions d'exécution :
 *  - `reserveDao.count() < THRESHOLD`
 *  - Connexion internet active
 *
 * Stratégie :
 *  1. Récupère des mots candidats depuis les **catégories Wiktionnaire** (registre soutenu,
 *     philosophie, rhétorique, etc.) via [WiktionnaireCategorySource].
 *  2. Si la source catégories est vide (réseau indisponible au moment du fetch),
 *     bascule sur la liste statique de secours [RareWordsCandidates.CANDIDATES].
 *  3. Filtre les mots déjà présents (réserve + collection) — anti-doublon.
 *  4. Pour chaque candidat, interroge [DictionaryService] pour obtenir la définition complète.
 *  5. S'arrête dès que `count() >= THRESHOLD`.
 *  6. Silencieux en cas d'erreur sur un mot (passe au suivant).
 */
class RefillWordReserveUseCase(
    private val reserveDao: WordReserveDao,
    private val flashcardDao: FlashcardDao,
    private val dictionaryService: DictionaryService,
    private val context: Context,
    private val categorySource: WiktionnaireCategorySource = WiktionnaireCategorySource()
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

        // ── Construire l'anti-doublon ─────────────────────────────────────────
        val existingInReserve    = reserveDao.getAllMots().map { normalize(it) }.toHashSet()
        val existingInFlashcards = flashcardDao.getAllMots().map { normalize(it) }.toHashSet()
        val alreadyKnown = existingInReserve + existingInFlashcards

        // ── Obtenir les candidats (Wiktionnaire catégories → fallback statique) ──
        val marginFactor = 3   // on demande 3× le nécessaire pour absorber les échecs réseau
        var candidates = categorySource
            .fetchCandidateWords(maxWords = needed * marginFactor)
            .filter { normalize(it) !in alreadyKnown }

        if (candidates.isEmpty()) {
            Log.w(TAG, "Catégories Wiktionnaire vides — bascule sur la liste statique de secours.")
            candidates = RareWordsCandidates.CANDIDATES
                .shuffled()
                .filter { normalize(it) !in alreadyKnown }
        }

        if (candidates.isEmpty()) {
            Log.w(TAG, "Tous les candidats (dynamiques + statiques) sont déjà connus.")
            return
        }

        Log.d(TAG, "${candidates.size} candidats disponibles après filtrage doublon.")

        // ── Fetch définitions + insertion ─────────────────────────────────────
        var addedCount = 0

        for (word in candidates) {
            if (reserveDao.count() >= THRESHOLD) break

            try {
                val results = dictionaryService.searchWord(word)
                val best = results.firstOrNull { it.definition.isNotBlank() } ?: continue

                reserveDao.insert(
                    WordReserveEntity(
                        id = UUID.randomUUID().toString(),
                        mot = best.mot.trim(),
                        definition = best.definition.trim(),
                        synonymes = best.synonymes,
                        exemples = best.exemples,
                        categorieGrammaticale = best.categorieGrammaticale,
                        fromApi = true
                    )
                )
                addedCount++
                Log.d(TAG, "Ajouté : ${best.mot} ($addedCount/$needed)")

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
