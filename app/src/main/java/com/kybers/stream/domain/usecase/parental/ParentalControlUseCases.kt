package com.kybers.stream.domain.usecase.parental

import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.ParentalControlRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetParentalSettingsUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    operator fun invoke(): Flow<ParentalControlSettings> = repository.getSettings()
}

class SetParentalPinUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(pin: String): Result<Unit> {
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("El PIN debe tener 4 dígitos"))
        }
        return repository.setPin(pin)
    }
}

class VerifyParentalPinUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(pin: String): Result<Boolean> = repository.verifyPin(pin)
}

class AuthenticateParentalSessionUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(pin: String): Result<ParentalSession> = 
        repository.authenticateSession(pin)
}

class GetParentalSessionUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    operator fun invoke(): Flow<ParentalSession> = repository.getCurrentSession()
}

class ValidateContentParentalUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(
        contentId: String,
        title: String,
        description: String?,
        categoryId: String?,
        categoryName: String?,
        contentType: ContentType,
        rating: ContentRating = ContentRating.ALL
    ): Result<ParentalValidationResult> {
        val analysis = ContentAnalysis(
            contentId = contentId,
            title = title,
            description = description,
            categoryId = categoryId,
            categoryName = categoryName,
            rating = rating,
            contentType = contentType
        )
        return repository.validateContent(analysis)
    }
}

class ManageBlockedCategoriesUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend fun addCategory(categoryId: String): Result<Unit> = 
        repository.addBlockedCategory(categoryId)
    
    suspend fun removeCategory(categoryId: String): Result<Unit> = 
        repository.removeBlockedCategory(categoryId)
}

class ManageBlockedKeywordsUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend fun addKeyword(keyword: String): Result<Unit> {
        if (keyword.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("La palabra clave no puede estar vacía"))
        }
        return repository.addBlockedKeyword(keyword.trim())
    }
    
    suspend fun removeKeyword(keyword: String): Result<Unit> = 
        repository.removeBlockedKeyword(keyword.trim())
}

class SetMaxRatingUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(rating: ContentRating): Result<Unit> = 
        repository.setMaxRating(rating)
}

class SetupParentalRecoveryUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(question: String, answer: String): Result<Unit> {
        if (question.trim().isEmpty() || answer.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("La pregunta y respuesta son obligatorias"))
        }
        return repository.setRecoveryQuestion(question.trim(), answer.trim())
    }
}

class RecoverParentalPinUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(answer: String): Result<String> {
        val isCorrect = repository.verifyRecoveryAnswer(answer).getOrElse { 
            return Result.failure(it) 
        }
        
        return if (isCorrect) {
            repository.resetPinWithRecovery()
        } else {
            Result.failure(SecurityException("Respuesta de recuperación incorrecta"))
        }
    }
}

class DisableParentalControlUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(pin: String): Result<Unit> {
        val isValid = repository.verifyPin(pin).getOrElse { 
            return Result.failure(it) 
        }
        
        return if (isValid) {
            repository.clearPin()
        } else {
            Result.failure(SecurityException("PIN incorrecto"))
        }
    }
}

class ExtendParentalSessionUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(additionalMinutes: Int = 30): Result<Unit> = 
        repository.extendSession(additionalMinutes)
}

class InvalidateParentalSessionUseCase @Inject constructor(
    private val repository: ParentalControlRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.invalidateSession()
}