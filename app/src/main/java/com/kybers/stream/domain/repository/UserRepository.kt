package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.AuthResult
import com.kybers.stream.domain.model.LoginRequest
import com.kybers.stream.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<UserProfile?>
    fun getSavedProfiles(): Flow<List<UserProfile>>
    suspend fun saveProfile(profile: UserProfile)
    suspend fun deleteProfile(profileId: String)
    suspend fun setActiveProfile(profileId: String)
    suspend fun clearAllProfiles()
    suspend fun authenticateUser(request: LoginRequest): AuthResult
    fun isUserLoggedIn(): Flow<Boolean>
}