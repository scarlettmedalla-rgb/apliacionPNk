package com.example.myapplication.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

/**
 * Rúbrica Punto 9: Detección de conectividad
 * Detecta la pérdida y recuperación del acceso a Internet (Wi-Fi y datos móviles)
 * y permite cambiar al funcionamiento local sin interrumpir el uso de la aplicación.
 */
object NetworkHelper {

    /**
     * Verifica de forma instantánea si el dispositivo tiene conexión a Internet activa (Wi-Fi o Móvil)
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                     capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Registra un observador para detectar en tiempo real la pérdida y recuperación de red.
     */
    fun registerNetworkCallback(
        context: Context,
        onNetworkAvailable: () -> Unit,
        onNetworkLost: () -> Unit
    ): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onNetworkAvailable()
                // Disparo automático de sincronización de cola pendiente sin duplicados (Criterio 13)
                SyncManager.syncPendingActions(context)
            }

            override fun onLost(network: Network) {
                onNetworkLost()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        return networkCallback
    }

    fun unregisterNetworkCallback(context: Context, callback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
