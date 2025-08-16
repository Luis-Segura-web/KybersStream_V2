package com.kybers.stream.domain.model

/**
 * Tipos de errores de red unificados
 */
sealed class NetworkError(
    override val message: String,
    val userMessage: String,
    val isRetryable: Boolean = true,
    val suggestedAction: String? = null
) : Throwable(message) {
    
    // Errores de autenticación
    data class AuthenticationError(
        val errorMessage: String = "Error de autenticación",
        val serverMessage: String? = null
    ) : NetworkError(
        message = errorMessage,
        userMessage = "Las credenciales no son válidas. Verifique usuario y contraseña.",
        isRetryable = false,
        suggestedAction = "Inicie sesión nuevamente"
    )
    
    data class AuthorizationError(
        val errorMessage: String = "Error de autorización"
    ) : NetworkError(
        message = errorMessage,
        userMessage = "No tiene permisos para acceder a este contenido.",
        isRetryable = false,
        suggestedAction = "Contacte con su proveedor de servicio"
    )
    
    // Errores de servidor
    data class ServerError(
        val statusCode: Int,
        val errorMessage: String = "Error del servidor"
    ) : NetworkError(
        message = "HTTP $statusCode: $errorMessage",
        userMessage = when (statusCode) {
            500 -> "Error interno del servidor. Intente más tarde."
            502 -> "Servicio no disponible temporalmente."
            503 -> "Servicio en mantenimiento. Intente más tarde."
            else -> "Error del servidor ($statusCode). Intente más tarde."
        },
        isRetryable = statusCode in 500..599,
        suggestedAction = "Reintentar en unos minutos"
    )
    
    // Errores de conexión
    object NetworkUnavailable : NetworkError(
        message = "Red no disponible",
        userMessage = "No hay conexión a internet. Verifique su conectividad.",
        isRetryable = true,
        suggestedAction = "Verificar conexión Wi-Fi o datos móviles"
    )
    
    data class ConnectionTimeout(
        val timeoutMs: Long
    ) : NetworkError(
        message = "Tiempo de espera agotado (${timeoutMs}ms)",
        userMessage = "La conexión tardó demasiado. Verifique su velocidad de internet.",
        isRetryable = true,
        suggestedAction = "Reintentar con mejor conexión"
    )
    
    data class ConnectionFailed(
        val host: String,
        val errorMessage: String
    ) : NetworkError(
        message = "No se pudo conectar a $host: $errorMessage",
        userMessage = "No se puede conectar al servidor. Verifique la URL del servidor.",
        isRetryable = true,
        suggestedAction = "Verificar URL del servidor en configuración"
    )
    
    // Errores de contenido
    data class ContentNotFound(
        val contentId: String,
        val contentType: String
    ) : NetworkError(
        message = "Contenido no encontrado: $contentType/$contentId",
        userMessage = "El contenido solicitado no está disponible.",
        isRetryable = false,
        suggestedAction = "Seleccionar otro contenido"
    )
    
    data class StreamNotFound(
        val streamId: String
    ) : NetworkError(
        message = "Stream no encontrado: $streamId",
        userMessage = "El canal o stream no está disponible en este momento.",
        isRetryable = true,
        suggestedAction = "Probar otro canal o formato"
    )
    
    // Errores de límites
    data class ConnectionLimitExceeded(
        val currentConnections: Int,
        val maxConnections: Int
    ) : NetworkError(
        message = "Límite de conexiones excedido: $currentConnections/$maxConnections",
        userMessage = "Se ha alcanzado el límite máximo de conexiones simultáneas ($maxConnections).",
        isRetryable = false,
        suggestedAction = "Cerrar otras sesiones activas"
    )
    
    data class AccountSuspended(
        val reason: String?
    ) : NetworkError(
        message = "Cuenta suspendida: ${reason ?: "Sin especificar"}",
        userMessage = "Su cuenta ha sido suspendida. ${reason ?: ""}",
        isRetryable = false,
        suggestedAction = "Contactar al proveedor de servicio"
    )
    
    // Errores de formato
    data class UnsupportedFormat(
        val mimeType: String?
    ) : NetworkError(
        message = "Formato no soportado: ${mimeType ?: "desconocido"}",
        userMessage = "El formato de video no es compatible con este dispositivo.",
        isRetryable = false,
        suggestedAction = "Probar otro canal o contactar soporte técnico"
    )
    
    data class CorruptedStream(
        val streamUrl: String
    ) : NetworkError(
        message = "Stream corrupto o inválido",
        userMessage = "El stream está dañado o no es válido.",
        isRetryable = true,
        suggestedAction = "Reintentar o probar otro canal"
    )
    
    // Errores de parsing
    data class ParseError(
        val dataType: String,
        val errorMessage: String
    ) : NetworkError(
        message = "Error al procesar $dataType: $errorMessage",
        userMessage = "Error al procesar la respuesta del servidor.",
        isRetryable = true,
        suggestedAction = "Reintentar la operación"
    )
    
    // Error genérico
    data class Unknown(
        val originalError: Throwable
    ) : NetworkError(
        message = "Error desconocido: ${originalError.message}",
        userMessage = "Ocurrió un error inesperado.",
        isRetryable = true,
        suggestedAction = "Reintentar la operación"
    )
}

/**
 * Mapper para convertir excepciones en errores de dominio
 */
object NetworkErrorMapper {
    
    fun mapError(throwable: Throwable): NetworkError {
        return when (throwable) {
            is NetworkError -> throwable
            
            // Errores HTTP
            is androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException -> {
                mapHttpError(throwable.responseCode, throwable.message)
            }
            
            // Errores de red de Retrofit
            is retrofit2.HttpException -> {
                mapHttpError(throwable.code(), throwable.message())
            }
            
            // Errores de conectividad
            is java.net.UnknownHostException -> {
                NetworkError.ConnectionFailed(
                    host = throwable.message ?: "servidor",
                    errorMessage = "Host no encontrado"
                )
            }
            
            is java.net.SocketTimeoutException -> {
                NetworkError.ConnectionTimeout(30000) // Default timeout
            }
            
            is java.net.ConnectException -> {
                NetworkError.ConnectionFailed(
                    host = "servidor",
                    errorMessage = throwable.message ?: "Conexión rechazada"
                )
            }
            
            is java.io.IOException -> {
                if (throwable.message?.contains("network", ignoreCase = true) == true) {
                    NetworkError.NetworkUnavailable
                } else {
                    NetworkError.Unknown(throwable)
                }
            }
            
            // Errores de parsing JSON
            is kotlinx.serialization.SerializationException,
            is com.google.gson.JsonSyntaxException -> {
                NetworkError.ParseError(
                    dataType = "JSON",
                    errorMessage = throwable.message ?: "Formato inválido"
                )
            }
            
            // Errores de seguridad
            is javax.net.ssl.SSLException -> {
                NetworkError.ConnectionFailed(
                    host = "servidor",
                    errorMessage = "Error de certificado SSL"
                )
            }
            
            else -> NetworkError.Unknown(throwable)
        }
    }
    
    private fun mapHttpError(statusCode: Int, message: String?): NetworkError {
        return when (statusCode) {
            401 -> NetworkError.AuthenticationError(
                errorMessage = message ?: "No autorizado",
                serverMessage = message
            )
            
            403 -> NetworkError.AuthorizationError(
                errorMessage = message ?: "Prohibido"
            )
            
            404 -> {
                // Distinguir entre contenido y stream no encontrado
                if (message?.contains("stream", ignoreCase = true) == true) {
                    NetworkError.StreamNotFound("desconocido")
                } else {
                    NetworkError.ContentNotFound("desconocido", "contenido")
                }
            }
            
            429 -> NetworkError.ConnectionLimitExceeded(
                currentConnections = 1,
                maxConnections = 1
            )
            
            in 500..599 -> NetworkError.ServerError(
                statusCode = statusCode,
                errorMessage = message ?: "Error del servidor"
            )
            
            else -> NetworkError.Unknown(
                RuntimeException("HTTP $statusCode: $message")
            )
        }
    }
}

/**
 * Configuración de reintentos
 */
data class RetryConfig(
    val maxAttempts: Int = 3,
    val baseDelayMs: Long = 1000L,
    val maxDelayMs: Long = 30000L,
    val backoffMultiplier: Double = 2.0,
    val retryableErrors: Set<Class<out NetworkError>> = setOf(
        NetworkError.NetworkUnavailable::class.java,
        NetworkError.ConnectionTimeout::class.java,
        NetworkError.ConnectionFailed::class.java,
        NetworkError.ServerError::class.java,
        NetworkError.ParseError::class.java,
        NetworkError.CorruptedStream::class.java
    )
) {
    fun shouldRetry(error: NetworkError, attempt: Int): Boolean {
        return attempt < maxAttempts && 
               error.isRetryable && 
               retryableErrors.any { it.isInstance(error) }
    }
    
    fun getDelayMs(attempt: Int): Long {
        val delay = (baseDelayMs * Math.pow(backoffMultiplier, attempt.toDouble())).toLong()
        return minOf(delay, maxDelayMs)
    }
}