package com.example.lexicaandroid2.features.auth.presentation

import android.content.Context
import android.content.Intent
import com.example.lexicaandroid2.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

object GoogleSignInHelper {
    fun createClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .build()

        return GoogleSignIn.getClient(context, options)
    }

    fun extractIdToken(data: Intent?): Result<String> = runCatching {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            .getResult(ApiException::class.java)

        account.idToken?.takeIf { it.isNotBlank() }
            ?: error("Token Google introuvable")
    }
}
