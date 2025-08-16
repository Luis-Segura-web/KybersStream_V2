package com.kybers.stream.domain.model

import java.time.LocalDateTime

/**
 * Configuración de control parental
 */
data class ParentalControlSettings(
    val isEnabled: Boolean = false,
    val pinHash: String = "", // PIN cifrado con salt
    val salt: String = "",
    val autoLockMinutes: Int = 30, // Auto-bloqueo tras N minutos
    val blockedCategories: Set<String> = emptySet(),
    val blockedKeywords: Set<String> = emptySet(),
    val maxRating: ContentRating = ContentRating.ALL,
    val recoveryQuestion: String = "",
    val recoveryAnswerHash: String = "",
    val failedAttempts: Int = 0,
    val lastFailedAttempt: Long = 0L,
    val lockoutUntil: Long = 0L
) {
    val isLockedOut: Boolean
        get() = System.currentTimeMillis() < lockoutUntil
    
    val lockoutRemainingSeconds: Long
        get() = maxOf(0, (lockoutUntil - System.currentTimeMillis()) / 1000)
}

/**
 * Clasificaciones de contenido
 */
enum class ContentRating(val displayName: String, val minAge: Int) {
    ALL("Todo público", 0),
    G("General", 0),
    PG("Orientación parental", 7),
    PG13("Mayores de 13", 13),
    R("Restringido", 17),
    NC17("Solo adultos", 18),
    ADULT("Contenido adulto", 21);
    
    companion object {
        fun fromString(value: String?): ContentRating {
            return values().find { 
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: ALL
        }
    }
}

/**
 * Sesión de autenticación parental
 */
data class ParentalSession(
    val isAuthenticated: Boolean = false,
    val authenticatedAt: Long = 0L,
    val expiresAt: Long = 0L
) {
    val isValid: Boolean
        get() = isAuthenticated && System.currentTimeMillis() < expiresAt
    
    val remainingMinutes: Long
        get() = maxOf(0, (expiresAt - System.currentTimeMillis()) / (60 * 1000))
}

/**
 * Resultado de validación de control parental
 */
sealed class ParentalValidationResult {
    object Allowed : ParentalValidationResult()
    object Blocked : ParentalValidationResult()
    data class RequiresPin(val reason: String) : ParentalValidationResult()
    data class Lockout(val remainingSeconds: Long) : ParentalValidationResult()
}

/**
 * Razones de bloqueo
 */
enum class BlockReason(val message: String) {
    BLOCKED_CATEGORY("Categoría bloqueada por control parental"),
    BLOCKED_KEYWORD("Contenido bloqueado por palabras clave"),
    RATING_EXCEEDED("Clasificación no permitida para el perfil actual"),
    SESSION_EXPIRED("Sesión de control parental expirada"),
    PARENTAL_CONTROL_ENABLED("Control parental activado")
}

/**
 * Datos para análisis de contenido
 */
data class ContentAnalysis(
    val contentId: String,
    val title: String,
    val description: String?,
    val categoryId: String?,
    val categoryName: String?,
    val rating: ContentRating = ContentRating.ALL,
    val contentType: ContentType
) {
    /**
     * Verifica si el contenido contiene palabras clave bloqueadas
     */
    fun containsBlockedKeywords(keywords: Set<String>): Boolean {
        if (keywords.isEmpty()) return false
        
        val searchText = "$title ${description ?: ""}".lowercase()
        return keywords.any { keyword ->
            searchText.contains(keyword.lowercase())
        }
    }
    
    /**
     * Verifica si la categoría está bloqueada
     */
    fun isCategoryBlocked(blockedCategories: Set<String>): Boolean {
        if (blockedCategories.isEmpty()) return false
        
        return blockedCategories.any { blocked ->
            categoryId?.equals(blocked, ignoreCase = true) == true ||
            categoryName?.equals(blocked, ignoreCase = true) == true
        }
    }
}