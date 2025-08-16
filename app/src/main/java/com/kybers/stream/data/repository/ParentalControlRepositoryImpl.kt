package com.kybers.stream.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.ParentalControlRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ParentalControlRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ParentalControlRepository {

    companion object {
        private val IS_ENABLED = booleanPreferencesKey("parental_control_enabled")
        private val PIN_HASH = stringPreferencesKey("parental_pin_hash")
        private val PIN_SALT = stringPreferencesKey("parental_pin_salt")
        private val AUTO_LOCK_MINUTES = intPreferencesKey("parental_auto_lock_minutes")
        private val BLOCKED_CATEGORIES = stringSetPreferencesKey("parental_blocked_categories")
        private val BLOCKED_KEYWORDS = stringSetPreferencesKey("parental_blocked_keywords")
        private val MAX_RATING = stringPreferencesKey("parental_max_rating")
        private val RECOVERY_QUESTION = stringPreferencesKey("parental_recovery_question")
        private val RECOVERY_ANSWER_HASH = stringPreferencesKey("parental_recovery_answer_hash")
        private val FAILED_ATTEMPTS = intPreferencesKey("parental_failed_attempts")
        private val LAST_FAILED_ATTEMPT = longPreferencesKey("parental_last_failed_attempt")
        private val LOCKOUT_UNTIL = longPreferencesKey("parental_lockout_until")
        
        // Sesión (en memoria para mayor seguridad)
        private val SESSION_AUTHENTICATED = booleanPreferencesKey("parental_session_auth")
        private val SESSION_AUTHENTICATED_AT = longPreferencesKey("parental_session_auth_at")
        private val SESSION_EXPIRES_AT = longPreferencesKey("parental_session_expires_at")
        
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutos
    }

    override fun getSettings(): Flow<ParentalControlSettings> {
        return dataStore.data.map { preferences ->
            ParentalControlSettings(
                isEnabled = preferences[IS_ENABLED] ?: false,
                pinHash = preferences[PIN_HASH] ?: "",
                salt = preferences[PIN_SALT] ?: "",
                autoLockMinutes = preferences[AUTO_LOCK_MINUTES] ?: 30,
                blockedCategories = preferences[BLOCKED_CATEGORIES] ?: emptySet(),
                blockedKeywords = preferences[BLOCKED_KEYWORDS] ?: emptySet(),
                maxRating = ContentRating.fromString(preferences[MAX_RATING]),
                recoveryQuestion = preferences[RECOVERY_QUESTION] ?: "",
                recoveryAnswerHash = preferences[RECOVERY_ANSWER_HASH] ?: "",
                failedAttempts = preferences[FAILED_ATTEMPTS] ?: 0,
                lastFailedAttempt = preferences[LAST_FAILED_ATTEMPT] ?: 0L,
                lockoutUntil = preferences[LOCKOUT_UNTIL] ?: 0L
            )
        }
    }

    override suspend fun updateSettings(settings: ParentalControlSettings): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[IS_ENABLED] = settings.isEnabled
                preferences[AUTO_LOCK_MINUTES] = settings.autoLockMinutes
                preferences[BLOCKED_CATEGORIES] = settings.blockedCategories
                preferences[BLOCKED_KEYWORDS] = settings.blockedKeywords
                preferences[MAX_RATING] = settings.maxRating.name
                preferences[RECOVERY_QUESTION] = settings.recoveryQuestion
                preferences[RECOVERY_ANSWER_HASH] = settings.recoveryAnswerHash
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setPin(pin: String): Result<Unit> {
        return try {
            val salt = generateSalt()
            val hash = hashPin(pin, salt)
            
            dataStore.edit { preferences ->
                preferences[PIN_HASH] = hash
                preferences[PIN_SALT] = salt
                preferences[IS_ENABLED] = true
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPin(pin: String): Result<Boolean> {
        return try {
            val settings = getSettings().first()
            
            if (settings.isLockedOut) {
                return Result.success(false)
            }
            
            if (settings.pinHash.isEmpty() || settings.salt.isEmpty()) {
                return Result.success(false)
            }
            
            val hash = hashPin(pin, settings.salt)
            val isValid = hash == settings.pinHash
            
            if (!isValid) {
                recordFailedAttempt()
            } else {
                clearFailedAttempts()
            }
            
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearPin(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.remove(PIN_HASH)
                preferences.remove(PIN_SALT)
                preferences[IS_ENABLED] = false
            }
            invalidateSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentSession(): Flow<ParentalSession> {
        return dataStore.data.map { preferences ->
            ParentalSession(
                isAuthenticated = preferences[SESSION_AUTHENTICATED] ?: false,
                authenticatedAt = preferences[SESSION_AUTHENTICATED_AT] ?: 0L,
                expiresAt = preferences[SESSION_EXPIRES_AT] ?: 0L
            )
        }
    }

    override suspend fun authenticateSession(pin: String): Result<ParentalSession> {
        return try {
            val isValid = verifyPin(pin).getOrElse { return Result.failure(it) }
            
            if (isValid) {
                val settings = getSettings().first()
                val now = System.currentTimeMillis()
                val expiresAt = now + (settings.autoLockMinutes * 60 * 1000L)
                
                val session = ParentalSession(
                    isAuthenticated = true,
                    authenticatedAt = now,
                    expiresAt = expiresAt
                )
                
                dataStore.edit { preferences ->
                    preferences[SESSION_AUTHENTICATED] = true
                    preferences[SESSION_AUTHENTICATED_AT] = now
                    preferences[SESSION_EXPIRES_AT] = expiresAt
                }
                
                Result.success(session)
            } else {
                Result.failure(SecurityException("PIN incorrecto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun extendSession(minutes: Int): Result<Unit> {
        return try {
            val session = getCurrentSession().first()
            
            if (session.isValid) {
                val newExpiresAt = System.currentTimeMillis() + (minutes * 60 * 1000L)
                
                dataStore.edit { preferences ->
                    preferences[SESSION_EXPIRES_AT] = newExpiresAt
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun invalidateSession(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[SESSION_AUTHENTICATED] = false
                preferences[SESSION_AUTHENTICATED_AT] = 0L
                preferences[SESSION_EXPIRES_AT] = 0L
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addBlockedCategory(categoryId: String): Result<Unit> {
        return try {
            val settings = getSettings().first()
            val updatedCategories = settings.blockedCategories + categoryId
            
            dataStore.edit { preferences ->
                preferences[BLOCKED_CATEGORIES] = updatedCategories
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeBlockedCategory(categoryId: String): Result<Unit> {
        return try {
            val settings = getSettings().first()
            val updatedCategories = settings.blockedCategories - categoryId
            
            dataStore.edit { preferences ->
                preferences[BLOCKED_CATEGORIES] = updatedCategories
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addBlockedKeyword(keyword: String): Result<Unit> {
        return try {
            val settings = getSettings().first()
            val updatedKeywords = settings.blockedKeywords + keyword.trim().lowercase()
            
            dataStore.edit { preferences ->
                preferences[BLOCKED_KEYWORDS] = updatedKeywords
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeBlockedKeyword(keyword: String): Result<Unit> {
        return try {
            val settings = getSettings().first()
            val updatedKeywords = settings.blockedKeywords - keyword.trim().lowercase()
            
            dataStore.edit { preferences ->
                preferences[BLOCKED_KEYWORDS] = updatedKeywords
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setMaxRating(rating: ContentRating): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[MAX_RATING] = rating.name
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateContent(analysis: ContentAnalysis): Result<ParentalValidationResult> {
        return try {
            val settings = getSettings().first()
            val session = getCurrentSession().first()
            
            // Si el control parental está desactivado, permitir todo
            if (!settings.isEnabled) {
                return Result.success(ParentalValidationResult.Allowed)
            }
            
            // Verificar lockout
            if (settings.isLockedOut) {
                return Result.success(ParentalValidationResult.Lockout(settings.lockoutRemainingSeconds))
            }
            
            // Verificar sesión válida
            if (!session.isValid) {
                return Result.success(ParentalValidationResult.RequiresPin("Sesión de control parental expirada"))
            }
            
            // Verificar categoría bloqueada
            if (analysis.isCategoryBlocked(settings.blockedCategories)) {
                return Result.success(ParentalValidationResult.Blocked)
            }
            
            // Verificar palabras clave bloqueadas
            if (analysis.containsBlockedKeywords(settings.blockedKeywords)) {
                return Result.success(ParentalValidationResult.Blocked)
            }
            
            // Verificar clasificación
            if (analysis.rating.minAge > settings.maxRating.minAge) {
                return Result.success(ParentalValidationResult.Blocked)
            }
            
            Result.success(ParentalValidationResult.Allowed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isContentBlocked(contentId: String, contentType: ContentType): Result<Boolean> {
        return try {
            // Esta implementación sería más completa con acceso a metadatos del contenido
            val settings = getSettings().first()
            Result.success(settings.isEnabled && !getCurrentSession().first().isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setRecoveryQuestion(question: String, answer: String): Result<Unit> {
        return try {
            val salt = generateSalt()
            val answerHash = hashPin(answer.trim().lowercase(), salt)
            
            dataStore.edit { preferences ->
                preferences[RECOVERY_QUESTION] = question
                preferences[RECOVERY_ANSWER_HASH] = answerHash
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyRecoveryAnswer(answer: String): Result<Boolean> {
        return try {
            val settings = getSettings().first()
            
            if (settings.recoveryAnswerHash.isEmpty()) {
                return Result.success(false)
            }
            
            val answerHash = hashPin(answer.trim().lowercase(), settings.salt)
            Result.success(answerHash == settings.recoveryAnswerHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPinWithRecovery(): Result<String> {
        return try {
            // Generar PIN temporal de 6 dígitos
            val temporaryPin = (100000..999999).random().toString()
            setPin(temporaryPin).getOrElse { return Result.failure(it) }
            
            // Limpiar intentos fallidos
            clearFailedAttempts()
            
            Result.success(temporaryPin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordFailedAttempt(): Result<Unit> {
        return try {
            val settings = getSettings().first()
            val newAttempts = settings.failedAttempts + 1
            val now = System.currentTimeMillis()
            
            dataStore.edit { preferences ->
                preferences[FAILED_ATTEMPTS] = newAttempts
                preferences[LAST_FAILED_ATTEMPT] = now
                
                // Aplicar lockout si se excede el límite
                if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                    preferences[LOCKOUT_UNTIL] = now + LOCKOUT_DURATION_MS
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearFailedAttempts(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[FAILED_ATTEMPTS] = 0
                preferences[LAST_FAILED_ATTEMPT] = 0L
                preferences[LOCKOUT_UNTIL] = 0L
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isInLockout(): Result<Boolean> {
        return try {
            val settings = getSettings().first()
            Result.success(settings.isLockedOut)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllSettings(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.remove(IS_ENABLED)
                preferences.remove(PIN_HASH)
                preferences.remove(PIN_SALT)
                preferences.remove(AUTO_LOCK_MINUTES)
                preferences.remove(BLOCKED_CATEGORIES)
                preferences.remove(BLOCKED_KEYWORDS)
                preferences.remove(MAX_RATING)
                preferences.remove(RECOVERY_QUESTION)
                preferences.remove(RECOVERY_ANSWER_HASH)
                preferences.remove(FAILED_ATTEMPTS)
                preferences.remove(LAST_FAILED_ATTEMPT)
                preferences.remove(LOCKOUT_UNTIL)
                preferences.remove(SESSION_AUTHENTICATED)
                preferences.remove(SESSION_AUTHENTICATED_AT)
                preferences.remove(SESSION_EXPIRES_AT)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPin(pin: String, salt: String): String {
        val input = "$pin$salt"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}