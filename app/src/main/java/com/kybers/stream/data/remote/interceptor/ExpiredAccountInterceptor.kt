package com.kybers.stream.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Interceptor que detecta respuestas HTML que indican que la cuenta ha expirado
 * y las convierte en errores HTTP apropiados
 */
class ExpiredAccountInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Solo verificar respuestas exitosas que podrían ser HTML de error
        if (response.isSuccessful && 
            response.header("content-type")?.contains("text/html", ignoreCase = true) == true) {
            
            val responseBody = response.body
            if (responseBody != null) {
                val bodyString = responseBody.string()
                
                // Detectar diferentes patrones de cuentas expiradas
                val expiredPatterns = listOf(
                    "EXPIRED" to "cuenta expirada",
                    "Line has expired" to "línea expirada",
                    "XUI.ONE - Debug Mode" to "servicio no disponible",
                    "auth.*0" to "autenticación fallida"
                )
                
                for ((pattern, description) in expiredPatterns) {
                    if (bodyString.contains(pattern, ignoreCase = true)) {
                        // Crear una respuesta de error apropiada
                        val errorBody = """
                            {
                                "error": "account_expired",
                                "message": "Tu suscripción ha expirado. Por favor contacta a tu proveedor de servicios.",
                                "details": "$description"
                            }
                        """.trimIndent()
                        
                        return response.newBuilder()
                            .code(401) // Unauthorized
                            .message("Account Expired")
                            .body(errorBody.toResponseBody("application/json".toMediaType()))
                            .build()
                    }
                }
                
                // Si no es un error conocido, reconstruir la respuesta original
                return response.newBuilder()
                    .body(bodyString.toResponseBody(responseBody.contentType()))
                    .build()
            }
        }
        
        return response
    }
}
