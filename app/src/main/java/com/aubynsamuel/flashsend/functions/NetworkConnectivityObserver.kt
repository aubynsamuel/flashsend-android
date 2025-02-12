package com.aubynsamuel.flashsend.functions

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Represents different network states.
sealed class ConnectivityStatus {
    object Available : ConnectivityStatus()
    object Unavailable : ConnectivityStatus()
}

// Define an interface for the observer.
interface ConnectivityObserver {
    fun observe(): Flow<ConnectivityStatus>
}

// Implementation of the ConnectivityObserver using ConnectivityManager.
class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityStatus> = callbackFlow {
        // Create a network callback that sends network status changes to the Flow.
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectivityStatus.Available).isSuccess
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityStatus.Unavailable).isSuccess
            }

            // Optional: handle changes in network capabilities.
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val status =
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                        ConnectivityStatus.Available
                    else
                        ConnectivityStatus.Unavailable

                trySend(status).isSuccess
            }
        }

        // Build a network request that listens for internet-capable networks.
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Clean up when no longer needed.
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
