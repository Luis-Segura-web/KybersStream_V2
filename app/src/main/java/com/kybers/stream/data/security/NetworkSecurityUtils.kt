package com.kybers.stream.data.security

import android.net.Uri
import java.net.InetAddress
import java.net.URL
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tipos de validación de URL
 */
enum class UrlValidationType {
    IPTV_SERVER,    // Servidor IPTV (más permisivo)
    API_ENDPOINT,   // Endpoint de API (más estricto)
    STREAM_URL,     // URL de stream (específico para streaming)
    TELEMETRY       // Endpoint de telemetría (solo HTTPS)
}

/**
 * Resultado de validación de seguridad
 */
sealed class SecurityValidationResult {
    object Valid : SecurityValidationResult()
    data class Invalid(val reason: String, val suggestion: String? = null) : SecurityValidationResult()
    data class Warning(val reason: String, val allowAnyway: Boolean = false) : SecurityValidationResult()
}

/**
 * Utilidades para validación de seguridad de red
 */
@Singleton
class NetworkSecurityUtils @Inject constructor() {
    
    companion object {
        // Patrones de URLs peligrosas
        private val DANGEROUS_SCHEMES = setOf("javascript", "data", "file", "ftp")
        
        // Puertos comúnmente seguros
        private val SAFE_PORTS = setOf(80, 443, 8080, 8443, 8000, 8888, 1935, 554)
        
        // Puertos comunes para IPTV
        private val IPTV_PORTS = setOf(80, 443, 8080, 8443, 25461, 25462, 25463, 8000, 8001, 1935, 554)
        
        // Patrones de IPs privadas (RFC 1918)
        private val PRIVATE_IP_PATTERNS = listOf(
            Pattern.compile("^10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"),
            Pattern.compile("^172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3}$"),
            Pattern.compile("^192\\.168\\.\\d{1,3}\\.\\d{1,3}$"),
            Pattern.compile("^127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
        )
        
        // Dominios sospechosos o conocidos por malware
        private val SUSPICIOUS_DOMAINS = setOf(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl",
            "short.link", "rb.gy", "cutt.ly"
        )
        
        // Extensiones de archivo permitidas para streams
        private val ALLOWED_STREAM_EXTENSIONS = setOf(
            "m3u8", "ts", "mp4", "avi", "mkv", "flv", 
            "mov", "wmv", "webm", "m4v", "3gp"
        )
        
        // TLDs comúnmente seguros
        private val SAFE_TLDS = setOf(
            "com", "org", "net", "edu", "gov", "mil",
            "int", "info", "biz", "name", "pro"
        )
    }
    
    /**
     * Valida una URL según el tipo especificado
     */
    fun validateUrl(url: String, type: UrlValidationType): SecurityValidationResult {
        if (url.isBlank()) {
            return SecurityValidationResult.Invalid("URL vacía", "Proporcione una URL válida")
        }
        
        return try {
            val uri = Uri.parse(url)
            val parsedUrl = URL(url)
            
            // Validaciones básicas
            validateBasicSecurity(uri, parsedUrl)?.let { return it }
            
            // Validaciones específicas por tipo
            when (type) {
                UrlValidationType.IPTV_SERVER -> validateIptvServer(uri, parsedUrl)
                UrlValidationType.API_ENDPOINT -> validateApiEndpoint(uri, parsedUrl)
                UrlValidationType.STREAM_URL -> validateStreamUrl(uri, parsedUrl)
                UrlValidationType.TELEMETRY -> validateTelemetryEndpoint(uri, parsedUrl)
            }
        } catch (e: Exception) {
            SecurityValidationResult.Invalid(
                "URL malformada: ${e.message}",
                "Verifique el formato de la URL"
            )
        }
    }
    
    /**
     * Validaciones básicas de seguridad
     */
    private fun validateBasicSecurity(uri: Uri, url: URL): SecurityValidationResult? {
        // Verificar esquema peligroso
        if (uri.scheme?.lowercase() in DANGEROUS_SCHEMES) {
            return SecurityValidationResult.Invalid(
                "Esquema de URL peligroso: ${uri.scheme}",
                "Use HTTP o HTTPS únicamente"
            )
        }
        
        // Verificar que tenga esquema
        if (uri.scheme.isNullOrBlank()) {
            return SecurityValidationResult.Invalid(
                "URL sin esquema de protocolo",
                "Agregue http:// o https:// al inicio"
            )
        }
        
        // Verificar host
        if (uri.host.isNullOrBlank()) {
            return SecurityValidationResult.Invalid(
                "URL sin host válido",
                "Proporcione un dominio o IP válida"
            )
        }
        
        // Verificar dominios sospechosos
        if (uri.host in SUSPICIOUS_DOMAINS) {
            return SecurityValidationResult.Warning(
                "Dominio de acortador de URLs detectado: ${uri.host}",
                allowAnyway = true
            )
        }
        
        return null
    }
    
    /**
     * Valida servidor IPTV (más permisivo)
     */
    private fun validateIptvServer(uri: Uri, url: URL): SecurityValidationResult {
        // IPTV permite HTTP para compatibilidad
        if (uri.scheme !in setOf("http", "https")) {
            return SecurityValidationResult.Invalid(
                "Protocolo no válido para servidor IPTV: ${uri.scheme}",
                "Use http:// o https://"
            )
        }
        
        // Verificar puerto si está especificado
        val port = uri.port
        if (port != -1 && port !in IPTV_PORTS) {
            return SecurityValidationResult.Warning(
                "Puerto inusual para IPTV: $port",
                allowAnyway = true
            )
        }
        
        // Advertencia para HTTP en producción
        if (uri.scheme == "http" && !isPrivateNetwork(uri.host!!)) {
            return SecurityValidationResult.Warning(
                "Conexión HTTP no encriptada a servidor público",
                allowAnyway = true
            )
        }
        
        return SecurityValidationResult.Valid
    }
    
    /**
     * Valida endpoint de API (más estricto)
     */
    private fun validateApiEndpoint(uri: Uri, url: URL): SecurityValidationResult {
        // APIs deben usar HTTPS en producción
        if (uri.scheme != "https" && !isPrivateNetwork(uri.host!!)) {
            return SecurityValidationResult.Invalid(
                "APIs públicas deben usar HTTPS",
                "Cambie http:// por https://"
            )
        }
        
        // Verificar puerto
        val port = uri.port
        if (port != -1 && port !in SAFE_PORTS) {
            return SecurityValidationResult.Warning(
                "Puerto inusual para API: $port",
                allowAnyway = false
            )
        }
        
        return SecurityValidationResult.Valid
    }
    
    /**
     * Valida URL de stream
     */
    private fun validateStreamUrl(uri: Uri, url: URL): SecurityValidationResult {
        val path = uri.path ?: ""
        
        // Verificar extensión de archivo si está presente
        val extension = path.substringAfterLast('.', "").lowercase()
        if (extension.isNotEmpty() && extension !in ALLOWED_STREAM_EXTENSIONS) {
            return SecurityValidationResult.Warning(
                "Extensión de archivo inusual para stream: .$extension",
                allowAnyway = true
            )
        }
        
        // Streams pueden usar HTTP para compatibilidad
        if (uri.scheme !in setOf("http", "https", "rtmp", "rtsp")) {
            return SecurityValidationResult.Invalid(
                "Protocolo no soportado para streaming: ${uri.scheme}",
                "Use http, https, rtmp o rtsp"
            )
        }
        
        return SecurityValidationResult.Valid
    }
    
    /**
     * Valida endpoint de telemetría (solo HTTPS)
     */
    private fun validateTelemetryEndpoint(uri: Uri, url: URL): SecurityValidationResult {
        // Telemetría debe ser siempre HTTPS
        if (uri.scheme != "https") {
            return SecurityValidationResult.Invalid(
                "Telemetría requiere conexión segura (HTTPS)",
                "Use https:// para proteger los datos"
            )
        }
        
        // Verificar TLD seguro
        val host = uri.host!!
        val tld = host.substringAfterLast('.', "").lowercase()
        if (tld !in SAFE_TLDS && !isPrivateNetwork(host)) {
            return SecurityValidationResult.Warning(
                "TLD inusual para telemetría: .$tld",
                allowAnyway = false
            )
        }
        
        return SecurityValidationResult.Valid
    }
    
    /**
     * Verifica si una dirección es de red privada
     */
    private fun isPrivateNetwork(host: String): Boolean {
        // Verificar localhost
        if (host.equals("localhost", ignoreCase = true)) {
            return true
        }
        
        // Verificar IPs privadas
        return PRIVATE_IP_PATTERNS.any { pattern ->
            pattern.matcher(host).matches()
        }
    }
    
    /**
     * Sanitiza una URL removiendo información sensible
     */
    fun sanitizeUrlForLogging(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val builder = Uri.Builder()
                .scheme(uri.scheme)
                .authority(uri.host + if (uri.port != -1) ":${uri.port}" else "")
                .path(uri.path)
            
            // No incluir query parameters que pueden contener credenciales
            if (!uri.query.isNullOrBlank()) {
                builder.appendQueryParameter("query", "***REDACTED***")
            }
            
            builder.build().toString()
        } catch (e: Exception) {
            "***INVALID_URL***"
        }
    }
    
    /**
     * Extrae información de seguridad de una URL
     */
    fun getUrlSecurityInfo(url: String): Map<String, String> {
        return try {
            val uri = Uri.parse(url)
            mapOf(
                "scheme" to (uri.scheme ?: "unknown"),
                "host" to (uri.host ?: "unknown"),
                "port" to if (uri.port != -1) uri.port.toString() else "default",
                "isPrivate" to isPrivateNetwork(uri.host ?: "").toString(),
                "isSecure" to (uri.scheme == "https").toString(),
                "path" to (uri.path?.let { if (it.length > 50) "${it.take(47)}..." else it } ?: "/")
            )
        } catch (e: Exception) {
            mapOf("error" to e.message.orEmpty())
        }
    }
    
    /**
     * Genera una URL segura agregando parámetros de seguridad si es necesario
     */
    fun makeUrlSecure(url: String, forceHttps: Boolean = false): String {
        return try {
            val uri = Uri.parse(url)
            
            if (forceHttps && uri.scheme == "http" && !isPrivateNetwork(uri.host!!)) {
                uri.buildUpon()
                    .scheme("https")
                    .build()
                    .toString()
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }
    
    /**
     * Verifica si una URL necesita advertencias de seguridad
     */
    fun needsSecurityWarning(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            uri.scheme == "http" && !isPrivateNetwork(uri.host!!)
        } catch (e: Exception) {
            true
        }
    }
}