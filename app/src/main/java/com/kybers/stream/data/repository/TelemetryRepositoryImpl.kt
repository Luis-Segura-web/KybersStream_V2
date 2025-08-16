package com.kybers.stream.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.TelemetryRepository
import com.kybers.stream.data.cache.CacheManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val cacheManager: CacheManager
) : TelemetryRepository {

    companion object {
        private val TELEMETRY_ENABLED = booleanPreferencesKey("telemetry_enabled")
        private val COLLECT_PERFORMANCE = booleanPreferencesKey("telemetry_collect_performance")
        private val COLLECT_USAGE = booleanPreferencesKey("telemetry_collect_usage")
        private val COLLECT_CRASH = booleanPreferencesKey("telemetry_collect_crash")
        private val DATA_RETENTION_DAYS = intPreferencesKey("telemetry_retention_days")
        private val LAST_CONSENT_DATE = longPreferencesKey("telemetry_last_consent")
        private val CURRENT_SESSION_ID = stringPreferencesKey("telemetry_current_session")
        
        private const val MAX_EVENTS_PER_BATCH = 100
        private const val MAX_RETENTION_DAYS = 90
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val pendingEvents = ConcurrentLinkedQueue<TelemetryEvent>()
    private val pendingMetrics = ConcurrentLinkedQueue<PerformanceMetrics>()
    private val pendingUsage = ConcurrentLinkedQueue<FeatureUsage>()
    private val pendingQuality = ConcurrentLinkedQueue<StreamQualityMetrics>()
    
    private val _uploadState = MutableStateFlow<TelemetryUploadState>(TelemetryUploadState.Idle)
    
    private var currentSessionId: String? = null

    override fun getSettings(): Flow<TelemetrySettings> {
        return dataStore.data.map { preferences ->
            TelemetrySettings(
                isEnabled = preferences[TELEMETRY_ENABLED] ?: false,
                collectPerformanceData = preferences[COLLECT_PERFORMANCE] ?: false,
                collectUsageData = preferences[COLLECT_USAGE] ?: false,
                collectCrashData = preferences[COLLECT_CRASH] ?: true,
                dataRetentionDays = preferences[DATA_RETENTION_DAYS] ?: 30,
                lastConsentDate = preferences[LAST_CONSENT_DATE] ?: 0L
            )
        }
    }

    override suspend fun updateSettings(settings: TelemetrySettings): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[TELEMETRY_ENABLED] = settings.isEnabled
                preferences[COLLECT_PERFORMANCE] = settings.collectPerformanceData
                preferences[COLLECT_USAGE] = settings.collectUsageData
                preferences[COLLECT_CRASH] = settings.collectCrashData
                preferences[DATA_RETENTION_DAYS] = minOf(settings.dataRetentionDays, MAX_RETENTION_DAYS)
            }
            
            // Si se deshabilita, limpiar datos pendientes
            if (!settings.isEnabled) {
                clearPendingData()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setUserConsent(granted: Boolean): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            dataStore.edit { preferences ->
                preferences[TELEMETRY_ENABLED] = granted
                preferences[LAST_CONSENT_DATE] = now
                
                if (granted) {
                    // Habilitar colección básica por defecto
                    preferences[COLLECT_CRASH] = true
                    preferences[COLLECT_PERFORMANCE] = false
                    preferences[COLLECT_USAGE] = false
                } else {
                    // Deshabilitar todo
                    preferences[COLLECT_PERFORMANCE] = false
                    preferences[COLLECT_USAGE] = false
                    preferences[COLLECT_CRASH] = false
                }
            }
            
            if (!granted) {
                clearPendingData()
                deleteAllUserData()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isConsentRequired(): Boolean {
        val settings = getSettings().first()
        return settings.needsConsent
    }

    override suspend fun recordEvent(event: TelemetryEvent): Result<Unit> {
        return try {
            val settings = getSettings().first()
            
            if (!TelemetryFilters.shouldCollectEvent(event, settings)) {
                return Result.success(Unit)
            }
            
            // Filtrar datos sensibles
            val sanitizedEvent = event.copy(
                properties = TelemetryFilters.filterSensitiveProperties(event.properties),
                deviceInfo = DeviceInfo.sanitized(event.deviceInfo)
            )
            
            // No recopilar si contiene datos sensibles y el filtro está habilitado
            if (sanitizedEvent.isSensitive()) {
                return Result.success(Unit)
            }
            
            pendingEvents.offer(sanitizedEvent)
            
            // Limitar cola en memoria
            while (pendingEvents.size > MAX_EVENTS_PER_BATCH * 2) {
                pendingEvents.poll()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordPerformanceMetric(metric: PerformanceMetrics): Result<Unit> {
        return try {
            val settings = getSettings().first()
            
            if (!settings.collectPerformanceData) {
                return Result.success(Unit)
            }
            
            pendingMetrics.offer(metric)
            
            // Limitar cola
            while (pendingMetrics.size > 50) {
                pendingMetrics.poll()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordFeatureUsage(usage: FeatureUsage): Result<Unit> {
        return try {
            val settings = getSettings().first()
            
            if (!settings.collectUsageData) {
                return Result.success(Unit)
            }
            
            pendingUsage.offer(usage)
            
            while (pendingUsage.size > 30) {
                pendingUsage.poll()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordStreamQuality(quality: StreamQualityMetrics): Result<Unit> {
        return try {
            val settings = getSettings().first()
            
            if (!settings.collectPerformanceData) {
                return Result.success(Unit)
            }
            
            pendingQuality.offer(quality)
            
            while (pendingQuality.size > 20) {
                pendingQuality.poll()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingEvents(): List<TelemetryEvent> {
        return pendingEvents.toList()
    }

    override suspend fun createBatch(): TelemetryBatch? {
        if (pendingEvents.isEmpty() && 
            pendingMetrics.isEmpty() && 
            pendingUsage.isEmpty() && 
            pendingQuality.isEmpty()) {
            return null
        }
        
        val events = mutableListOf<TelemetryEvent>()
        val metrics = mutableListOf<PerformanceMetrics>()
        val usage = mutableListOf<FeatureUsage>()
        val quality = mutableListOf<StreamQualityMetrics>()
        
        // Extraer eventos hasta el límite
        repeat(minOf(MAX_EVENTS_PER_BATCH, pendingEvents.size)) {
            pendingEvents.poll()?.let { events.add(it) }
        }
        
        // Extraer todas las métricas pendientes
        while (pendingMetrics.isNotEmpty()) {
            pendingMetrics.poll()?.let { metrics.add(it) }
        }
        
        while (pendingUsage.isNotEmpty()) {
            pendingUsage.poll()?.let { usage.add(it) }
        }
        
        while (pendingQuality.isNotEmpty()) {
            pendingQuality.poll()?.let { quality.add(it) }
        }
        
        return TelemetryBatch(
            events = events,
            performanceMetrics = metrics,
            featureUsage = usage,
            streamQualityMetrics = quality
        ).sanitize()
    }

    override suspend fun markBatchAsSent(batchId: String): Result<Unit> {
        return try {
            // En una implementación real, marcaríamos en base de datos local
            // Por ahora, solo actualizamos el estado
            _uploadState.value = TelemetryUploadState.Success(batchId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearOldData(): Result<Unit> {
        return try {
            val settings = getSettings().first()
            val cutoffTime = System.currentTimeMillis() - (settings.dataRetentionDays * 24 * 60 * 60 * 1000L)
            
            // Filtrar eventos antiguos
            val recentEvents = pendingEvents.filter { it.timestamp > cutoffTime }
            pendingEvents.clear()
            pendingEvents.addAll(recentEvents)
            
            // Filtrar métricas antiguas
            val recentMetrics = pendingMetrics.filter { it.timestamp > cutoffTime }
            pendingMetrics.clear()
            pendingMetrics.addAll(recentMetrics)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUploadState(): Flow<TelemetryUploadState> {
        return _uploadState
    }

    override suspend fun uploadBatch(batch: TelemetryBatch): Result<Unit> {
        return try {
            _uploadState.value = TelemetryUploadState.Uploading
            
            // En una implementación real, enviaríamos a un servidor de analytics
            // Por ahora, simular envío exitoso
            kotlinx.coroutines.delay(1000) // Simular latencia de red
            
            val success = true // Simular respuesta del servidor
            
            if (success) {
                _uploadState.value = TelemetryUploadState.Success(batch.batchId, System.currentTimeMillis())
                Result.success(Unit)
            } else {
                _uploadState.value = TelemetryUploadState.Failed("Error del servidor", 1)
                Result.failure(RuntimeException("Error al enviar telemetría"))
            }
        } catch (e: Exception) {
            _uploadState.value = TelemetryUploadState.Failed(e.message ?: "Error desconocido", 1)
            Result.failure(e)
        }
    }

    override suspend fun scheduleUpload(): Result<Unit> {
        return try {
            val batch = createBatch()
            if (batch != null && !batch.isEmpty) {
                uploadBatch(batch)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportUserData(): Result<String> {
        return try {
            val settings = getSettings().first()
            val data = mapOf(
                "settings" to settings,
                "pendingEvents" to pendingEvents.size,
                "pendingMetrics" to pendingMetrics.size,
                "pendingUsage" to pendingUsage.size,
                "pendingQuality" to pendingQuality.size,
                "currentSession" to currentSessionId
            )
            
            Result.success(json.encodeToString(data))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllUserData(): Result<Unit> {
        return try {
            clearPendingData()
            
            dataStore.edit { preferences ->
                preferences.remove(TELEMETRY_ENABLED)
                preferences.remove(COLLECT_PERFORMANCE)
                preferences.remove(COLLECT_USAGE)
                preferences.remove(COLLECT_CRASH)
                preferences.remove(DATA_RETENTION_DAYS)
                preferences.remove(LAST_CONSENT_DATE)
                preferences.remove(CURRENT_SESSION_ID)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDataSummary(): Result<Map<String, Any>> {
        return try {
            val settings = getSettings().first()
            val summary = mapOf(
                "telemetryEnabled" to settings.isEnabled,
                "dataRetentionDays" to settings.dataRetentionDays,
                "lastConsentDate" to settings.lastConsentDate,
                "pendingEventsCount" to pendingEvents.size,
                "pendingMetricsCount" to pendingMetrics.size,
                "currentSessionActive" to (currentSessionId != null)
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startSession(): String {
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId
        
        try {
            dataStore.edit { preferences ->
                preferences[CURRENT_SESSION_ID] = sessionId
            }
        } catch (e: Exception) {
            // Error no crítico, continuar con sesión en memoria
        }
        
        return sessionId
    }

    override suspend fun endSession(sessionId: String): Result<Unit> {
        return try {
            if (currentSessionId == sessionId) {
                currentSessionId = null
                
                dataStore.edit { preferences ->
                    preferences.remove(CURRENT_SESSION_ID)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentSessionId(): String? {
        return currentSessionId
    }
    
    private fun clearPendingData() {
        pendingEvents.clear()
        pendingMetrics.clear()
        pendingUsage.clear()
        pendingQuality.clear()
    }
}