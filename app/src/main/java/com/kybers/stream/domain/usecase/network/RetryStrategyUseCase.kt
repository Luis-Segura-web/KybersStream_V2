package com.kybers.stream.domain.usecase.network

import com.kybers.stream.domain.model.NetworkError
import com.kybers.stream.domain.model.RetryConfig
import com.kybers.stream.data.network.NetworkConnectivityManager
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estrategia de reintentos con backoff exponencial
 */
@Singleton
class RetryStrategyUseCase @Inject constructor(
    private val connectivityManager: NetworkConnectivityManager
) {
    
    /**
     * Ejecuta una operación con estrategia de reintento automático
     */
    suspend fun <T> executeWithRetry(
        config: RetryConfig = RetryConfig(),
        operation: suspend () -> T
    ): Result<T> {
        var lastError: Throwable? = null
        var attempt = 0
        
        while (attempt < config.maxAttempts) {
            try {
                // Verificar conectividad antes del intento
                if (!connectivityManager.isNetworkAvailable()) {
                    return Result.failure(NetworkError.NetworkUnavailable)
                }
                
                // Ejecutar operación
                val result = operation()
                return Result.success(result)
                
            } catch (error: Throwable) {
                lastError = error
                val networkError = com.kybers.stream.domain.model.NetworkErrorMapper.mapError(error)
                
                // Verificar si el error es reintentable
                if (!config.shouldRetry(networkError, attempt + 1)) {
                    return Result.failure(networkError)
                }
                
                // Esperar con backoff exponencial
                if (attempt < config.maxAttempts - 1) {
                    val delayMs = config.getDelayMs(attempt)
                    delay(delayMs)
                }
                
                attempt++
            }
        }
        
        return Result.failure(lastError ?: RuntimeException("Operación falló después de ${config.maxAttempts} intentos"))
    }
    
    /**
     * Ejecuta con reintentos y notificación de progreso
     */
    suspend fun <T> executeWithRetryAndProgress(
        config: RetryConfig = RetryConfig(),
        onRetry: (attempt: Int, error: NetworkError, nextDelayMs: Long) -> Unit = { _, _, _ -> },
        operation: suspend () -> T
    ): Result<T> {
        var lastError: Throwable? = null
        var attempt = 0
        
        while (attempt < config.maxAttempts) {
            try {
                // Verificar conectividad
                if (!connectivityManager.isNetworkAvailable()) {
                    return Result.failure(NetworkError.NetworkUnavailable)
                }
                
                val result = operation()
                return Result.success(result)
                
            } catch (error: Throwable) {
                lastError = error
                val networkError = com.kybers.stream.domain.model.NetworkErrorMapper.mapError(error)
                
                if (!config.shouldRetry(networkError, attempt + 1)) {
                    return Result.failure(networkError)
                }
                
                // Notificar reintento
                if (attempt < config.maxAttempts - 1) {
                    val delayMs = config.getDelayMs(attempt)
                    onRetry(attempt + 1, networkError, delayMs)
                    delay(delayMs)
                }
                
                attempt++
            }
        }
        
        return Result.failure(lastError ?: RuntimeException("Máximo número de reintentos alcanzado"))
    }
    
    /**
     * Configuraciones predefinidas de reintento
     */
    object Configs {
        
        val FAST = RetryConfig(
            maxAttempts = 2,
            baseDelayMs = 500L,
            maxDelayMs = 2000L,
            backoffMultiplier = 1.5
        )
        
        val NORMAL = RetryConfig(
            maxAttempts = 3,
            baseDelayMs = 1000L,
            maxDelayMs = 10000L,
            backoffMultiplier = 2.0
        )
        
        val AGGRESSIVE = RetryConfig(
            maxAttempts = 5,
            baseDelayMs = 2000L,
            maxDelayMs = 30000L,
            backoffMultiplier = 2.5
        )
        
        val STREAMING = RetryConfig(
            maxAttempts = 3,
            baseDelayMs = 1500L,
            maxDelayMs = 15000L,
            backoffMultiplier = 2.0,
            retryableErrors = setOf(
                NetworkError.NetworkUnavailable::class.java,
                NetworkError.ConnectionTimeout::class.java,
                NetworkError.ConnectionFailed::class.java,
                NetworkError.ServerError::class.java,
                NetworkError.CorruptedStream::class.java
            )
        )
        
        val AUTH = RetryConfig(
            maxAttempts = 2, // Menos reintentos para auth
            baseDelayMs = 1000L,
            maxDelayMs = 5000L,
            backoffMultiplier = 2.0,
            retryableErrors = setOf(
                NetworkError.NetworkUnavailable::class.java,
                NetworkError.ConnectionTimeout::class.java,
                NetworkError.ServerError::class.java
            )
        )
    }
}

/**
 * Extension para usar reintentos de manera más simple
 */
suspend inline fun <T> withRetry(
    retryStrategy: RetryStrategyUseCase,
    config: RetryConfig = RetryConfig(),
    crossinline operation: suspend () -> T
): Result<T> {
    return retryStrategy.executeWithRetry(config) { operation() }
}

/**
 * Extension para reintentos con notificación
 */
suspend inline fun <T> withRetryAndProgress(
    retryStrategy: RetryStrategyUseCase,
    config: RetryConfig = RetryConfig(),
    noinline onRetry: (attempt: Int, error: NetworkError, nextDelayMs: Long) -> Unit = { _, _, _ -> },
    crossinline operation: suspend () -> T
): Result<T> {
    return retryStrategy.executeWithRetryAndProgress(config, onRetry) { operation() }
}