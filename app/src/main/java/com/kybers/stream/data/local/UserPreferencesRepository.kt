package com.kybers.stream.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val USER_SERVER = stringPreferencesKey("user_server")
        private val USER_USERNAME = stringPreferencesKey("user_username")
        private val USER_PASSWORD = stringPreferencesKey("user_password")
        private val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val LAST_LOGIN_TIME = longPreferencesKey("last_login_time")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun saveUserCredentials(
        server: String,
        username: String,
        password: String,
        displayName: String = username
    ) {
        dataStore.edit { preferences ->
            preferences[USER_SERVER] = server
            preferences[USER_USERNAME] = username
            preferences[USER_PASSWORD] = password
            preferences[USER_DISPLAY_NAME] = displayName
            preferences[IS_LOGGED_IN] = true
            preferences[LAST_LOGIN_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun clearUserCredentials() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}