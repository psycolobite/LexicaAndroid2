# TACHE_S2 - Fix Recherche (API externe + accents)

Date: 2026-03-09
Statut: Code feature pret pour integration

## Objectif

Completer le fix de recherche avec:
- fallback accent-insensitive (ex: `cafe` trouve `café`)
- fallback API externe (Wiktionnaire) si aucun resultat local

## Fichiers modifies

- `app/src/main/java/com/example/lexicaandroid2/domain/repository/SearchRepository.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/repository/SearchRepositoryImpl.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/search/SearchViewModel.kt`

## Changements

1) `SearchRepository`
- Ajout de la methode:
  - `suspend fun searchExternal(query: String): List<Flashcard>`

2) `SearchRepositoryImpl`
- Injection optionnelle de `DictionaryService` (default `DictionaryServiceImpl()`)
- Implementation de `searchExternal(...)`
- Mapping `WordResult` -> `Flashcard` (id prefixe `external:`)

3) `SearchViewModel`
- Conserve le fix S1 (annulation job precedent)
- Si recherche locale vide et query non vide:
  - fallback local accent-insensitive (normalisation Unicode, suppression diacritiques)
  - puis fallback API externe pour `GLOBAL` et `BY_WORD`
- `clearSearch()` vide aussi `results` et `totalResults`

## Notes

- Aucun changement SQL/DAO
- Aucun changement UI requis
- Aucun changement de route/navigation requis
