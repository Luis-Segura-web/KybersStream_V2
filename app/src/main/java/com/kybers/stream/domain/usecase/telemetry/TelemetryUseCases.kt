package com.kybers.stream.domain.usecase.telemetry

import android.content.Context
import android.os.Build
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTelemetrySettingsUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    operator fun invoke(): Flow<TelemetrySettings> = repository.getSettings()
}

class SetTelemetryConsentUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(granted: Boolean): Result<Unit> = 
        repository.setUserConsent(granted)
}

class CheckConsentRequiredUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(): Boolean = repository.isConsentRequired()
}

class RecordAppEventUseCase @Inject constructor(
    private val repository: TelemetryRepository,
    private val context: Context
) {
    suspend operator fun invoke(
        eventType: TelemetryEventType,
        category: String,
        action: String,
        label: String? = null,
        value: Double? = null,
        properties: Map<String, String> = emptyMap(),
        severity: TelemetrySeverity = TelemetrySeverity.INFO
    ): Result<Unit> {
        val sessionId = repository.getCurrentSessionId() ?: return Result.success(Unit)
        
        val event = TelemetryEvent(
            eventType = eventType.name,
            severity = severity.name,
            category = category,
            action = action,
            label = label,
            value = value,
            properties = properties,
            sessionId = sessionId,
            appVersion = getAppVersion(),
            deviceInfo = createDeviceInfo()
        )
        
        return repository.recordEvent(event)
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun createDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appBuild = getAppVersion(),
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            screenResolution = getScreenResolution(),
            isTablet = isTabletDevice(),
            hasHardwareAcceleration = true,
            availableMemoryMB = getAvailableMemoryMB(),
            storageSpaceGB = getStorageSpaceGB(),
            networkType = "Unknown", // Se actualizaría con ConnectivityManager
            locale = java.util.Locale.getDefault().toString(),
            timezone = java.util.TimeZone.getDefault().id
        )
    }
    
    private fun getScreenResolution(): String {
        return try {
            val displayMetrics = context.resources.displayMetrics
            "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun isTabletDevice(): Boolean {
        return (context.resources.configuration.screenLayout and 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK) >= 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    private fun getAvailableMemoryMB(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            (memInfo.availMem / 1024 / 1024).toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getStorageSpaceGB(): Int {
        return try {
            val internal = context.filesDir.usableSpace / 1024 / 1024 / 1024
            internal.toInt()
        } catch (e: Exception) {
            0
        }
    }
}

class RecordPerformanceUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(
        category: String,
        memoryUsageMB: Int,
        cpuUsagePercent: Double = 0.0,
        networkLatencyMs: Long = 0L,
        frameDrops: Int = 0,
        bufferingEvents: Int = 0,
        loadTimeMs: Long = 0L
    ): Result<Unit> {
        val sessionId = repository.getCurrentSessionId() ?: return Result.success(Unit)
        
        val metric = PerformanceMetrics(
            sessionId = sessionId,
            memoryUsageMB = memoryUsageMB,
            cpuUsagePercent = cpuUsagePercent,
            networkLatencyMs = networkLatencyMs,
            frameDrops = frameDrops,
            bufferingEvents = bufferingEvents,
            loadTimeMs = loadTimeMs,
            category = category
        )
        
        return repository.recordPerformanceMetric(metric)
    }
}

class RecordFeatureUsageUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(
        featureName: String,
        usageCount: Int = 1,
        totalTimeMs: Long = 0L,
        successfulOperations: Int = 1,
        failedOperations: Int = 0,
        userContext: String? = null
    ): Result<Unit> {
        val sessionId = repository.getCurrentSessionId() ?: return Result.success(Unit)
        
        val usage = FeatureUsage(
            sessionId = sessionId,
            featureName = featureName,
            usageCount = usageCount,
            totalTimeMs = totalTimeMs,
            successfulOperations = successfulOperations,
            failedOperations = failedOperations,
            userContext = userContext
        )
        
        return repository.recordFeatureUsage(usage)
    }
}

class RecordStreamQualityUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(
        contentType: String,
        resolution: String,
        bitrate: String,
        bufferingTimeMs: Long = 0L,
        totalWatchTimeMs: Long = 0L,
        rebufferingEvents: Int = 0,
        qualityChanges: Int = 0,
        startupTimeMs: Long = 0L,
        connectionQuality: String = "unknown",
        errorCount: Int = 0
    ): Result<Unit> {
        val sessionId = repository.getCurrentSessionId() ?: return Result.success(Unit)
        
        val quality = StreamQualityMetrics(
            sessionId = sessionId,
            contentType = contentType,
            resolution = resolution,
            bitrate = bitrate,
            bufferingTimeMs = bufferingTimeMs,
            totalWatchTimeMs = totalWatchTimeMs,
            rebufferingEvents = rebufferingEvents,
            qualityChanges = qualityChanges,
            startupTimeMs = startupTimeMs,
            connectionQuality = connectionQuality,
            errorCount = errorCount
        )
        
        return repository.recordStreamQuality(quality)
    }
}

class StartTelemetrySessionUseCase @Inject constructor(
    private val repository: TelemetryRepository,
    private val recordAppEvent: RecordAppEventUseCase
) {
    suspend operator fun invoke(): Result<String> {
        return try {
            val sessionId = repository.startSession()
            
            // Registrar evento de inicio de sesión
            recordAppEvent(
                eventType = TelemetryEventType.APP_START,
                category = "session",
                action = "start",
                label = sessionId
            )
            
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class EndTelemetrySessionUseCase @Inject constructor(
    private val repository: TelemetryRepository,
    private val recordAppEvent: RecordAppEventUseCase
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return try {
            // Registrar evento de fin de sesión
            recordAppEvent(
                eventType = TelemetryEventType.APP_STOP,
                category = "session",
                action = "end",
                label = sessionId
            )
            
            // Programar envío de datos pendientes
            repository.scheduleUpload()
            
            // Finalizar sesión
            repository.endSession(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UploadTelemetryDataUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.scheduleUpload()
    
    fun getUploadState(): Flow<TelemetryUploadState> = repository.getUploadState()
}

class ManageTelemetryDataUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend fun exportData(): Result<String> = repository.exportUserData()
    
    suspend fun deleteAllData(): Result<Unit> = repository.deleteAllUserData()
    
    suspend fun getDataSummary(): Result<Map<String, Any>> = repository.getDataSummary()
    
    suspend fun cleanupOldData(): Result<Unit> = repository.clearOldData()
}

class UpdateTelemetrySettingsUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(
        collectPerformance: Boolean? = null,
        collectUsage: Boolean? = null,
        collectCrash: Boolean? = null,
        dataRetentionDays: Int? = null
    ): Result<Unit> {
        return try {
            val currentSettings = repository.getSettings().first()
            
            val updatedSettings = currentSettings.copy(
                collectPerformanceData = collectPerformance ?: currentSettings.collectPerformanceData,
                collectUsageData = collectUsage ?: currentSettings.collectUsageData,
                collectCrashData = collectCrash ?: currentSettings.collectCrashData,
                dataRetentionDays = dataRetentionDays ?: currentSettings.dataRetentionDays
            )
            
            repository.updateSettings(updatedSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Helper para eventos comunes de la aplicación
 */
class TelemetryEventHelper @Inject constructor(
    private val recordAppEvent: RecordAppEventUseCase,
    private val recordFeatureUsage: RecordFeatureUsageUseCase,
    private val recordPerformance: RecordPerformanceUseCase
) {
    
    suspend fun recordScreenView(screenName: String) {
        recordAppEvent(
            eventType = TelemetryEventType.NAVIGATION,
            category = "navigation",
            action = "screen_view",
            label = screenName
        )
    }
    
    suspend fun recordUserAction(feature: String, action: String, success: Boolean = true) {
        recordAppEvent(
            eventType = TelemetryEventType.USER_ACTION,
            category = "user_interaction",
            action = action,
            label = feature,
            properties = mapOf("success" to success.toString())
        )
        
        recordFeatureUsage(
            featureName = feature,
            successfulOperations = if (success) 1 else 0,
            failedOperations = if (!success) 1 else 0
        )
    }
    
    suspend fun recordError(category: String, error: String, context: String? = null) {
        recordAppEvent(
            eventType = TelemetryEventType.ERROR_OCCURRED,
            category = category,
            action = "error",
            label = error,
            properties = context?.let { mapOf("context" to it) } ?: emptyMap(),
            severity = TelemetrySeverity.ERROR
        )
    }
    
    suspend fun recordLoadTime(feature: String, timeMs: Long) {
        recordPerformance(
            category = "performance",
            loadTimeMs = timeMs,
            memoryUsageMB = 0 // Se puede obtener dinámicamente
        )
        
        recordAppEvent(
            eventType = TelemetryEventType.PERFORMANCE_METRIC,
            category = "performance",
            action = "load_time",
            label = feature,
            value = timeMs.toDouble()
        )
    }
}