package com.kybers.stream.data.local.dao

import androidx.room.*
import com.kybers.stream.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreferencesEntity>>
    
    @Query("SELECT value FROM user_preferences WHERE key = :key")
    suspend fun getPreference(key: String): String?
    
    @Query("SELECT value FROM user_preferences WHERE key = :key")
    fun getPreferenceFlow(key: String): Flow<String?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: UserPreferencesEntity)
    
    @Query("DELETE FROM user_preferences WHERE key = :key")
    suspend fun removePreference(key: String)
    
    @Query("DELETE FROM user_preferences")
    suspend fun clearAllPreferences()
    
    @Transaction
    suspend fun setPreferences(preferences: Map<String, String>) {
        preferences.forEach { (key, value) ->
            setPreference(UserPreferencesEntity(key, value))
        }
    }
}