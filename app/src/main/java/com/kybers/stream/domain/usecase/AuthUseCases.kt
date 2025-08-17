package com.kybers.stream.domain.usecase

import com.kybers.stream.domain.model.AuthResult
import com.kybers.stream.domain.model.LoginRequest
import com.kybers.stream.domain.model.UserProfile
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.data.cache.DatabaseCacheManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val databaseCacheManager: DatabaseCacheManager
) {
    suspend operator fun invoke(request: LoginRequest): AuthResult {
        // Validar entrada
        if (request.server.isBlank() || request.username.isBlank() || request.password.isBlank()) {
            return AuthResult.Error("Todos los campos son obligatorios")
        }
        
        if (!isValidUrl(request.server)) {
            return AuthResult.Error("URL del servidor no válida")
        }
        
        return userRepository.authenticateUser(request)
    }
    
    /**
     * Verifica si necesita sincronizar datos después del login exitoso
     * @param userProfile El perfil de usuario autenticado
     * @return true si necesita sincronizar, false si el caché es válido
     */
    suspend fun needsSync(userProfile: UserProfile): Boolean {
        val userHash = databaseCacheManager.generateUserHash(
            userProfile.username, 
            userProfile.password, 
            userProfile.server
        )
        
        // Verificar si el caché es válido (menos de 12 horas)
        return !databaseCacheManager.isXtreamCacheValid(userHash)
    }
    
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile?> = userRepository.getCurrentUser()
}

class GetSavedProfilesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<UserProfile>> = userRepository.getSavedProfiles()
}

class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() {
        userRepository.clearAllProfiles()
    }
}

class IsUserLoggedInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Boolean> = userRepository.isUserLoggedIn()
}