package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.mapper.toDomain
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepositoryImpl(
    private val dao: FlashcardDao
) : SearchRepository {

    override fun searchByWord(query: String, limit: Int): Flow<List<Flashcard>> {
        return if (query.isBlank()) {
            dao.getAllPaginated(limit, 0).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            dao.searchByWord(query, limit).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override fun searchByDefinition(query: String, limit: Int): Flow<List<Flashcard>> {
        return if (query.isBlank()) {
            dao.getAllPaginated(limit, 0).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            dao.searchByDefinition(query, limit).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override fun searchGlobal(query: String, limit: Int): Flow<List<Flashcard>> {
        return if (query.isBlank()) {
            dao.getAllPaginated(limit, 0).map { entities ->
                entities.map { it.toDomain() }
            }
        } else {
            dao.searchGlobal(query, limit).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override fun searchFavorites(query: String, limit: Int): Flow<List<Flashcard>> {
        return if (query.isBlank()) {
            dao.getAllPaginated(limit, 0).map { entities ->
                entities.filter { it.favori }.map { it.toDomain() }
            }
        } else {
            dao.searchFavorites(query, limit).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override fun getAllPaginated(limit: Int, offset: Int): Flow<List<Flashcard>> {
        return dao.getAllPaginated(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun countSearchResults(query: String): Int {
        return if (query.isBlank()) {
            dao.count()
        } else {
            dao.countSearchResults(query)
        }
    }
}
