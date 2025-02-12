package com.aubynsamuel.flashsend.functions

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.HttpURLConnection
import java.net.URL

// Represents different network states.
sealed class ConnectivityStatus {
    object Available : ConnectivityStatus() // Internet is reachable
    object Unavailable : ConnectivityStatus() // Internet is unreachable
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
                checkInternetReachability { isReachable ->
                    trySend(if (isReachable) ConnectivityStatus.Available else ConnectivityStatus.Unavailable).isSuccess
                }
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityStatus.Unavailable).isSuccess
            }

            // Optional: handle changes in network capabilities.
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                checkInternetReachability { isReachable ->
                    trySend(if (isReachable) ConnectivityStatus.Available else ConnectivityStatus.Unavailable).isSuccess
                }
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

    // Helper function to check if the internet is reachable.
    private fun checkInternetReachability(callback: (Boolean) -> Unit) {
        val url = URL("https://www.google.com")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 5000 // 5 seconds timeout
        connection.readTimeout = 5000

        try {
            connection.connect()
            callback(connection.responseCode == HttpURLConnection.HTTP_OK)
            logger("connection", "Has Internet Access")
        } catch (e: Exception) {
            callback(false)
            logger("connection", "No Internet Access $e")
        } finally {
            connection.disconnect()
        }
    }
}