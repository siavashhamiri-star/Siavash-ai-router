package com.example.tavanacity.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.tavanacity.domain.model.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class NetworkMonitor(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _networkStatus = MutableStateFlow(evaluateCurrentState())
    open val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateStatus()
        }

        override fun onLost(network: Network) {
            updateStatus()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val status = evaluateCapabilities(networkCapabilities)
            scope.launch {
                _networkStatus.value = status
            }
        }
    }

    init {
        registerCallback()
    }

    private fun registerCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // Fail-safe: fallback to polling/evaluation without crash
            updateStatus()
        }
    }

    open fun updateStatus() {
        scope.launch {
            _networkStatus.value = evaluateCurrentState()
        }
    }

    open fun getCurrentStatus(): NetworkStatus {
        val current = evaluateCurrentState()
        _networkStatus.value = current
        return current
    }

    private fun evaluateCurrentState(): NetworkStatus {
        val cm = connectivityManager ?: return NetworkStatus.OFFLINE
        val activeNetwork = cm.activeNetwork ?: return NetworkStatus.OFFLINE
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus.OFFLINE
        return evaluateCapabilities(capabilities)
    }

    private fun evaluateCapabilities(capabilities: NetworkCapabilities): NetworkStatus {
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) {
            return NetworkStatus.OFFLINE
        }

        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps

        // If bandwidth is very low or not yet validated, mark as UNSTABLE
        return when {
            !isValidated && downstreamBandwidth > 0 && downstreamBandwidth < 300 -> NetworkStatus.UNSTABLE
            downstreamBandwidth in 1..250 -> NetworkStatus.UNSTABLE
            isValidated || downstreamBandwidth >= 250 -> NetworkStatus.ONLINE
            else -> NetworkStatus.ONLINE
        }
    }

    open fun unregister() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
    }
}
