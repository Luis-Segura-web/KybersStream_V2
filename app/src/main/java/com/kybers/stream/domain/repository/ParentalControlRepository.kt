package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ParentalControlRepository {
    
    // Configuración
    fun getSettings(): Flow<ParentalControlSettings>
    suspend fun updateSettings(settings: ParentalControlSettings): Result<Unit>
    suspend fun setPin(pin: String): Result<Unit>
    suspend fun verifyPin(pin: String): Result<Boolean>
    suspend fun clearPin(): Result<Unit>
    
    // Sesión
    fun getCurrentSession(): Flow<ParentalSession>
    suspend fun authenticateSession(pin: String): Result<ParentalSession>
    suspend fun extendSession(minutes: Int): Result<Unit>
    suspend fun invalidateSession(): Result<Unit>
    
    // Bloqueos
    suspend fun addBlockedCategory(categoryId: String): Result<Unit>
    suspend fun removeBlockedCategory(categoryId: String): Result<Unit>
    suspend fun addBlockedKeyword(keyword: String): Result<Unit>
    suspend fun removeBlockedKeyword(keyword: String): Result<Unit>
    suspend fun setMaxRating(rating: ContentRating): Result<Unit>
    
    // Validación
    suspend fun validateContent(analysis: ContentAnalysis): Result<ParentalValidationResult>
    suspend fun isContentBlocked(contentId: String, contentType: ContentType): Result<Boolean>
    
    // Recuperación
    suspend fun setRecoveryQuestion(question: String, answer: String): Result<Unit>
    suspend fun verifyRecoveryAnswer(answer: String): Result<Boolean>
    suspend fun resetPinWithRecovery(): Result<String> // Retorna PIN temporal
    
    // Intentos fallidos
    suspend fun recordFailedAttempt(): Result<Unit>
    suspend fun clearFailedAttempts(): Result<Unit>
    suspend fun isInLockout(): Result<Boolean>
    
    // Limpieza
    suspend fun clearAllSettings(): Result<Unit>
}