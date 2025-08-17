package com.kybers.stream.data.sync

import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.domain.repository.XtreamRepository
import com.kybers.stream.domain.model.XtreamResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val xtreamRepository: XtreamRepository,
    private val databaseCacheManager: DatabaseCacheManager
) {
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentSyncJob: Job? = null
    
    suspend fun performInitialSync(): Result<Unit> {
        return try {
            val user = userRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
            
            // Verificar si ya tenemos datos válidos en cache
            if (databaseCacheManager.isXtreamCacheValid(userHash)) {
                return Result.success(Unit)
            }
            
            // Limpiar cache expirado de otros usuarios
            databaseCacheManager.cleanupOtherUsersData(userHash)
            
            // Sincronizar datos completos
            syncAllData(userHash)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncAllData(userHash: String) = withContext(Dispatchers.IO) {
        val jobs = listOf(
            async { syncCategories(userHash) },
            async { syncMovies(userHash) },
            async { syncSeries(userHash) },
            async { syncChannels(userHash) }
        )
        
        // Esperar a que todos los trabajos terminen
        val results = jobs.awaitAll()
        
        // Contar elementos sincronizados
        val moviesCount = (results[1] as? List<*>)?.size ?: 0
        val seriesCount = (results[2] as? List<*>)?.size ?: 0
        val channelsCount = (results[3] as? List<*>)?.size ?: 0
        val categoriesCount = (results[0] as? List<*>)?.size ?: 0
        
        // Actualizar metadata de sincronización
        databaseCacheManager.updateXtreamSyncMetadata(
            userHash = userHash,
            moviesCount = moviesCount,
            seriesCount = seriesCount,
            channelsCount = channelsCount,
            categoriesCount = categoriesCount
        )
    }
    
    private suspend fun syncCategories(userHash: String): List<Any> {
        val categories = mutableListOf<Any>()
        
        // Sincronizar categorías de VOD
        when (val vodCategoriesResult = xtreamRepository.getVodCategories()) {
            is XtreamResult.Success -> {
                databaseCacheManager.cacheXtreamCategories(vodCategoriesResult.data, "vod", userHash)
                categories.addAll(vodCategoriesResult.data)
            }
            else -> { /* Log error but continue */ }
        }
        
        // Sincronizar categorías de Series
        when (val seriesCategoriesResult = xtreamRepository.getSeriesCategories()) {
            is XtreamResult.Success -> {
                databaseCacheManager.cacheXtreamCategories(seriesCategoriesResult.data, "series", userHash)
                categories.addAll(seriesCategoriesResult.data)
            }
            else -> { /* Log error but continue */ }
        }
        
        // Sincronizar categorías de Live TV
        when (val liveCategoriesResult = xtreamRepository.getLiveCategories()) {
            is XtreamResult.Success -> {
                databaseCacheManager.cacheXtreamCategories(liveCategoriesResult.data, "live", userHash)
                categories.addAll(liveCategoriesResult.data)
            }
            else -> { /* Log error but continue */ }
        }
        
        return categories
    }
    
    private suspend fun syncMovies(userHash: String): List<Any> {
        return when (val moviesResult = xtreamRepository.getVodStreams()) {
            is XtreamResult.Success -> {
                databaseCacheManager.cacheXtreamMovies(moviesResult.data, userHash)
                moviesResult.data
            }
            else -> emptyList()
        }
    }
    
    private suspend fun syncSeries(userHash: String): List<Any> {
        return when (val seriesResult = xtreamRepository.getSeries()) {
            is XtreamResult.Success -> {
                databaseCacheManager.cacheXtreamSeries(seriesResult.data, userHash)
                seriesResult.data
            }
            else -> emptyList()
        }
    }
    
    private suspend fun syncChannels(userHash: String): List<Any> {
        return when (val channelsResult = xtreamRepository.getLiveStreams()) {
            is XtreamResult.Success -> {
                databaseCacheManager.cacheXtreamChannels(channelsResult.data, userHash)
                channelsResult.data
            }
            else -> emptyList()
        }
    }
    
    fun startAutoSync() {
        currentSyncJob?.cancel()
        currentSyncJob = syncScope.launch {
            while (isActive) {
                try {
                    // Limpiar datos expirados cada hora
                    databaseCacheManager.cleanupExpiredData()
                    
                    // Verificar si necesitamos resincronizar
                    val user = userRepository.getCurrentUser().first()
                    if (user != null) {
                        val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
                        
                        if (!databaseCacheManager.isXtreamCacheValid(userHash)) {
                            // Cache expirado, resincronizar
                            syncAllData(userHash)
                        }
                    }
                    
                    // Esperar 1 hora antes del próximo chequeo
                    delay(60 * 60 * 1000L)
                } catch (e: Exception) {
                    // Log error pero continúa
                    delay(60 * 60 * 1000L) // Reintentar en 1 hora
                }
            }
        }
    }
    
    fun stopAutoSync() {
        currentSyncJob?.cancel()
        currentSyncJob = null
    }
    
    suspend fun invalidateCache() {
        val user = userRepository.getCurrentUser().first()
        if (user != null) {
            val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
            databaseCacheManager.invalidateXtreamCache(userHash)
        }
    }
    
    suspend fun getCacheInfo(): CacheInfo? {
        val user = userRepository.getCurrentUser().first() ?: return null
        val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
        
        val isValid = databaseCacheManager.isXtreamCacheValid(userHash)
        val validUntil = databaseCacheManager.getXtreamCacheValidUntil(userHash)
        
        return CacheInfo(
            isValid = isValid,
            validUntil = validUntil,
            userHash = userHash
        )
    }
}

data class CacheInfo(
    val isValid: Boolean,
    val validUntil: LocalDateTime?,
    val userHash: String
)