package com.kybers.stream.domain.model

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Configuración de telemetría y privacidad
 */
data class TelemetrySettings(
    val isEnabled: Boolean = false,
    val collectPerformanceData: Boolean = false,
    val collectUsageData: Boolean = false,
    val collectCrashData: Boolean = true, // Habilitado por defecto para estabilidad
    val dataRetentionDays: Int = 30,
    val lastConsentDate: Long = 0L
) {
    val needsConsent: Boolean
        get() = lastConsentDate == 0L || 
                (System.currentTimeMillis() - lastConsentDate) > (365 * 24 * 60 * 60 * 1000L) // 1 año
}

/**
 * Tipos de eventos de telemetría
 */
enum class TelemetryEventType {
    APP_START,
    APP_STOP,
    USER_ACTION,
    PERFORMANCE_METRIC,
    ERROR_OCCURRED,
    STREAM_QUALITY,
    CONTENT_INTERACTION,
    NAVIGATION,
    FEATURE_USAGE
}

/**
 * Nivel de severidad para eventos
 */
enum class TelemetrySeverity {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Evento de telemetría base
 */
@Serializable
data class TelemetryEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val severity: String = TelemetrySeverity.INFO.name,
    val category: String,
    val action: String,
    val label: String? = null,
    val value: Double? = null,
    val properties: Map<String, String> = emptyMap(),
    val sessionId: String,
    val appVersion: String,
    val platform: String = "Android",
    val deviceInfo: DeviceInfo
) {
    fun isSensitive(): Boolean {
        return properties.keys.any { key ->
            SENSITIVE_KEYS.any { sensitiveKey ->
                key.contains(sensitiveKey, ignoreCase = true)
            }
        } || SENSITIVE_CATEGORIES.contains(category.lowercase())
    }
    
    companion object {
        val SENSITIVE_KEYS = listOf(
            "password", "pin", "token", "auth", "credential", 
            "email", "phone", "ip", "mac", "serial", "imei"
        )
        
        private val SENSITIVE_CATEGORIES = listOf(
            "authentication", "personal", "location", "contact"
        )
    }
}

/**
 * Información anónima del dispositivo
 */
@Serializable
data class DeviceInfo(
    val osVersion: String,
    val appBuild: String,
    val deviceModel: String,
    val screenResolution: String,
    val isTablet: Boolean,
    val hasHardwareAcceleration: Boolean,
    val availableMemoryMB: Int,
    val storageSpaceGB: Int,
    val networkType: String,
    val locale: String,
    val timezone: String
) {
    companion object {
        fun sanitized(original: DeviceInfo): DeviceInfo {
            return original.copy(
                deviceModel = sanitizeDeviceModel(original.deviceModel),
                networkType = sanitizeNetworkType(original.networkType)
            )
        }
        
        private fun sanitizeDeviceModel(model: String): String {
            // Remover números de serie o identificadores únicos
            return model.replace(Regex("[0-9]{4,}"), "****")
                        .replace(Regex("\\b[A-Z0-9]{8,}\\b"), "****")
        }
        
        private fun sanitizeNetworkType(networkType: String): String {
            // Solo mantener tipo general, no detalles específicos
            return when {
                networkType.contains("wifi", ignoreCase = true) -> "WiFi"
                networkType.contains("cellular", ignoreCase = true) -> "Cellular"
                networkType.contains("ethernet", ignoreCase = true) -> "Ethernet"
                else -> "Unknown"
            }
        }
    }
}

/**
 * Métricas de rendimiento
 */
@Serializable
data class PerformanceMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,
    val memoryUsageMB: Int,
    val cpuUsagePercent: Double,
    val networkLatencyMs: Long,
    val frameDrops: Int,
    val bufferingEvents: Int,
    val loadTimeMs: Long,
    val category: String // e.g., "video_playback", "app_navigation"
)

/**
 * Datos de uso de características
 */
@Serializable
data class FeatureUsage(
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,
    val featureName: String,
    val usageCount: Int,
    val totalTimeMs: Long,
    val successfulOperations: Int,
    val failedOperations: Int,
    val userContext: String? = null // e.g., "first_time_user", "power_user"
)

/**
 * Datos de calidad de streaming
 */
@Serializable
data class StreamQualityMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,
    val contentType: String, // live, vod, series
    val resolution: String,
    val bitrate: String,
    val bufferingTimeMs: Long,
    val totalWatchTimeMs: Long,
    val rebufferingEvents: Int,
    val qualityChanges: Int,
    val startupTimeMs: Long,
    val connectionQuality: String,
    val errorCount: Int
)

/**
 * Batch de eventos para envío eficiente
 */
@Serializable
data class TelemetryBatch(
    val batchId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val events: List<TelemetryEvent>,
    val performanceMetrics: List<PerformanceMetrics> = emptyList(),
    val featureUsage: List<FeatureUsage> = emptyList(),
    val streamQualityMetrics: List<StreamQualityMetrics> = emptyList(),
    val checksum: String = ""
) {
    fun sanitize(): TelemetryBatch {
        return copy(
            events = events.filterNot { it.isSensitive() }
                          .map { it.copy(deviceInfo = DeviceInfo.sanitized(it.deviceInfo)) },
            performanceMetrics = performanceMetrics,
            featureUsage = featureUsage,
            streamQualityMetrics = streamQualityMetrics
        )
    }
    
    val isEmpty: Boolean
        get() = events.isEmpty() && 
                performanceMetrics.isEmpty() && 
                featureUsage.isEmpty() && 
                streamQualityMetrics.isEmpty()
}

/**
 * Estado de envío de telemetría
 */
sealed class TelemetryUploadState {
    object Idle : TelemetryUploadState()
    object Uploading : TelemetryUploadState()
    data class Success(val batchId: String, val timestamp: Long) : TelemetryUploadState()
    data class Failed(val error: String, val retryCount: Int) : TelemetryUploadState()
}

/**
 * Configuración de privacidad para telemetría
 */
data class PrivacyConfig(
    val anonymizeIpAddresses: Boolean = true,
    val excludeSensitiveData: Boolean = true,
    val dataRetentionDays: Int = 30,
    val allowCrossAppAnalytics: Boolean = false,
    val enableLocationData: Boolean = false,
    val enableBehavioralAnalytics: Boolean = false
)

/**
 * Filtros de datos para cumplimiento de privacidad
 */
object TelemetryFilters {
    
    fun shouldCollectEvent(event: TelemetryEvent, settings: TelemetrySettings): Boolean {
        if (!settings.isEnabled) return false
        
        return when (event.eventType) {
            TelemetryEventType.PERFORMANCE_METRIC.name -> settings.collectPerformanceData
            TelemetryEventType.USER_ACTION.name,
            TelemetryEventType.CONTENT_INTERACTION.name,
            TelemetryEventType.NAVIGATION.name,
            TelemetryEventType.FEATURE_USAGE.name -> settings.collectUsageData
            TelemetryEventType.ERROR_OCCURRED.name -> settings.collectCrashData
            else -> true
        }
    }
    
    fun filterSensitiveProperties(properties: Map<String, String>): Map<String, String> {
        return properties.filterKeys { key ->
            !TelemetryEvent.SENSITIVE_KEYS.any { sensitiveKey ->
                key.contains(sensitiveKey, ignoreCase = true)
            }
        }
    }
    
    fun anonymizeUserData(data: String): String {
        return data
            .replace(Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), "***@***.***")
            .replace(Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"), "***.***.***.**")
            .replace(Regex("\\b\\d{4,}\\b"), "****")
    }
}