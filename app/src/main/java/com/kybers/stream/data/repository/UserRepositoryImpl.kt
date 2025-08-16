package com.kybers.stream.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kybers.stream.data.remote.api.XtreamApi
import com.kybers.stream.domain.model.AuthErrorCode
import com.kybers.stream.domain.model.AuthResult
import com.kybers.stream.domain.model.LoginRequest
import com.kybers.stream.domain.model.ServerInfo
import com.kybers.stream.domain.model.UserProfile
import com.kybers.stream.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val retrofit: Retrofit
) : UserRepository {
    
    companion object {
        private val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        private val USER_SERVER = stringPreferencesKey("user_server")
        private val USER_USERNAME = stringPreferencesKey("user_username")
        private val USER_PASSWORD = stringPreferencesKey("user_password")
        private val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val LAST_LOGIN_TIME = longPreferencesKey("last_login_time")
        private val USER_ID = stringPreferencesKey("user_id")
    }
    
    override fun getCurrentUser(): Flow<UserProfile?> = dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN] ?: false
        if (!isLoggedIn) return@map null
        
        UserProfile(
            id = preferences[USER_ID] ?: "",
            server = preferences[USER_SERVER] ?: "",
            username = preferences[USER_USERNAME] ?: "",
            password = preferences[USER_PASSWORD] ?: "",
            displayName = preferences[USER_DISPLAY_NAME] ?: "",
            isActive = true,
            lastLoginTime = preferences[LAST_LOGIN_TIME] ?: 0L
        )
    }
    
    override fun getSavedProfiles(): Flow<List<UserProfile>> = dataStore.data.map { preferences ->
        val currentUser = if (preferences[IS_LOGGED_IN] == true) {
            UserProfile(
                id = preferences[USER_ID] ?: "",
                server = preferences[USER_SERVER] ?: "",
                username = preferences[USER_USERNAME] ?: "",
                password = preferences[USER_PASSWORD] ?: "",
                displayName = preferences[USER_DISPLAY_NAME] ?: "",
                isActive = true,
                lastLoginTime = preferences[LAST_LOGIN_TIME] ?: 0L
            )
        } else null
        
        listOfNotNull(currentUser)
    }
    
    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = profile.id
            preferences[USER_SERVER] = profile.server
            preferences[USER_USERNAME] = profile.username
            preferences[USER_PASSWORD] = profile.password
            preferences[USER_DISPLAY_NAME] = profile.displayName
            preferences[IS_LOGGED_IN] = true
            preferences[LAST_LOGIN_TIME] = System.currentTimeMillis()
        }
    }
    
    override suspend fun deleteProfile(profileId: String) {
        val currentUser = getCurrentUser().first()
        if (currentUser?.id == profileId) {
            clearAllProfiles()
        }
    }
    
    override suspend fun setActiveProfile(profileId: String) {
        // Para esta implementación simple, solo activamos si es el perfil actual
        val currentUser = getCurrentUser().first()
        if (currentUser?.id == profileId) {
            dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = true
            }
        }
    }
    
    override suspend fun clearAllProfiles() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    override suspend fun authenticateUser(request: LoginRequest): AuthResult {
        return try {
            // Crear una instancia de API específica para este servidor
            val serverRetrofit = retrofit.newBuilder()
                .baseUrl(if (request.server.endsWith("/")) request.server else "${request.server}/")
                .build()
            
            val api = serverRetrofit.create(XtreamApi::class.java)
            
            val response = api.authenticate(
                username = request.username,
                password = request.password
            )
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                
                if (authResponse?.userInfo?.auth == 1) {
                    val serverInfo = authResponse.serverInfo?.let { 
                        ServerInfo(
                            serverProtocol = it.serverProtocol,
                            serverVersion = "",
                            timestampNow = it.timestampNow,
                            timeNow = it.timeNow,
                            allowedOutputFormats = listOf("m3u8", "ts")
                        )
                    }
                    
                    val userProfile = UserProfile(
                        id = UUID.randomUUID().toString(),
                        server = request.server,
                        username = request.username,
                        password = request.password,
                        displayName = request.username,
                        isActive = true,
                        lastLoginTime = System.currentTimeMillis(),
                        serverInfo = serverInfo
                    )
                    
                    saveProfile(userProfile)
                    AuthResult.Success(userProfile)
                } else {
                    AuthResult.Error(
                        message = authResponse?.userInfo?.message ?: "Credenciales inválidas",
                        code = AuthErrorCode.INVALID_CREDENTIALS
                    )
                }
            } else {
                AuthResult.Error(
                    message = "Error del servidor: ${response.code()}",
                    code = AuthErrorCode.SERVER_ERROR
                )
            }
        } catch (e: UnknownHostException) {
            AuthResult.Error(
                message = "No se puede conectar al servidor",
                code = AuthErrorCode.NETWORK_ERROR
            )
        } catch (e: SocketTimeoutException) {
            AuthResult.Error(
                message = "Tiempo de espera agotado",
                code = AuthErrorCode.TIMEOUT
            )
        } catch (e: Exception) {
            AuthResult.Error(
                message = "Error inesperado: ${e.message}",
                code = AuthErrorCode.UNKNOWN
            )
        }
    }
    
    override fun isUserLoggedIn(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
}