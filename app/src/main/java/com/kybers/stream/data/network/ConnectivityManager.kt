package com.kybers.stream.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estados de conectividad de red
 */
data class NetworkState(
    val isConnected: Boolean = false,
    val isWifi: Boolean = false,
    val isCellular: Boolean = false,
    val isEthernet: Boolean = false,
    val isMetered: Boolean = false,
    val hasInternet: Boolean = false,
    val connectionQuality: ConnectionQuality = ConnectionQuality.UNKNOWN
) {
    val isOnline: Boolean get() = isConnected && hasInternet
    
    val connectionType: String get() = when {
        isWifi -> "WiFi"
        isCellular -> "Móvil"
        isEthernet -> "Ethernet"
        else -> "Desconocido"
    }
    
    val speedDescription: String get() = when (connectionQuality) {
        ConnectionQuality.EXCELLENT -> "Excelente"
        ConnectionQuality.GOOD -> "Buena"
        ConnectionQuality.FAIR -> "Regular"
        ConnectionQuality.POOR -> "Pobre"
        ConnectionQuality.UNKNOWN -> "Desconocida"
    }
}

enum class ConnectionQuality {
    EXCELLENT,  // > 10 Mbps
    GOOD,       // 2-10 Mbps
    FAIR,       // 0.5-2 Mbps
    POOR,       // < 0.5 Mbps
    UNKNOWN
}

@Singleton
class NetworkConnectivityManager @Inject constructor(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Observa el estado de conectividad en tiempo real
     */
    fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkState())
            }
            
            override fun onLost(network: Network) {
                trySend(getCurrentNetworkState())
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(getCurrentNetworkState())
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Emitir estado inicial
        trySend(getCurrentNetworkState())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Obtiene el estado actual de la red
     */
    fun getCurrentNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { 
            connectivityManager.getNetworkCapabilities(it) 
        }
        
        return if (capabilities != null) {
            NetworkState(
                isConnected = true,
                isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
                isEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET),
                isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
                hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                connectionQuality = estimateConnectionQuality(capabilities)
            )
        } else {
            NetworkState() // Sin conexión
        }
    }
    
    /**
     * Verifica si hay conectividad
     */
    fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Verifica si la conexión es de calidad para streaming
     */
    fun isGoodForStreaming(): Boolean {
        val state = getCurrentNetworkState()
        return state.isOnline && state.connectionQuality in listOf(
            ConnectionQuality.GOOD,
            ConnectionQuality.EXCELLENT
        )
    }
    
    /**
     * Verifica si la conexión es medida (datos móviles limitados)
     */
    fun isMeteredConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
    
    /**
     * Obtiene información de la red activa para debugging
     */
    fun getNetworkInfo(): String {
        val state = getCurrentNetworkState()
        return buildString {
            appendLine("Conectividad de Red:")
            appendLine("- Conectado: ${if (state.isConnected) "Sí" else "No"}")
            appendLine("- Internet: ${if (state.hasInternet) "Sí" else "No"}")
            appendLine("- Tipo: ${state.connectionType}")
            appendLine("- Calidad: ${state.speedDescription}")
            appendLine("- Medida: ${if (state.isMetered) "Sí" else "No"}")
        }
    }
    
    private fun estimateConnectionQuality(capabilities: NetworkCapabilities): ConnectionQuality {
        return try {
            // Intentar obtener información de ancho de banda
            val downstreamKbps = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                capabilities.linkDownstreamBandwidthKbps
            } else {
                0
            }
            
            when {
                downstreamKbps > 10000 -> ConnectionQuality.EXCELLENT // > 10 Mbps
                downstreamKbps > 2000 -> ConnectionQuality.GOOD      // 2-10 Mbps
                downstreamKbps > 500 -> ConnectionQuality.FAIR       // 0.5-2 Mbps
                downstreamKbps > 0 -> ConnectionQuality.POOR         // < 0.5 Mbps
                else -> {
                    // Fallback basado en tipo de conexión
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionQuality.GOOD
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionQuality.EXCELLENT
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionQuality.FAIR
                        else -> ConnectionQuality.UNKNOWN
                    }
                }
            }
        } catch (e: Exception) {
            ConnectionQuality.UNKNOWN
        }
    }
}

/**
 * Extension para usar en ViewModels
 */
suspend inline fun <T> executeWithConnectivity(
    connectivityManager: NetworkConnectivityManager,
    crossinline action: suspend () -> T
): Result<T> {
    return try {
        if (!connectivityManager.isNetworkAvailable()) {
            Result.failure(com.kybers.stream.domain.model.NetworkError.NetworkUnavailable)
        } else {
            Result.success(action())
        }
    } catch (e: Exception) {
        Result.failure(com.kybers.stream.domain.model.NetworkErrorMapper.mapError(e))
    }
}