package com.example.lexicaandroid2.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object ConnectivityChecker {

    /**
     * Retourne `true` si l'appareil dispose d'une connexion internet active
     * (Wi-Fi, cellulaire ou Ethernet) avec la capacité NET_CAPABILITY_INTERNET.
     */
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

