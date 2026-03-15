package com.example.lexicaandroid2.presentation.admin

/**
 * Configuration du Mode Administrateur.
 * Liste des adresses e-mail autorisées à accéder au mode admin.
 * Ce fichier ne contient pas de données sensibles — l'adresse est visible côté client uniquement.
 */
object AdminConfig {
    /**
     * Ensemble des adresses e-mail ayant accès au mode admin.
     */
    val ADMIN_EMAILS: Set<String> = setOf(
        "r0x.ef@outlook.fr"
    )

    fun isAdmin(email: String?): Boolean = email != null && email in ADMIN_EMAILS
}

