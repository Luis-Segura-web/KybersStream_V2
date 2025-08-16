package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    
    // Configuración
    fun getSettings(): Flow<TelemetrySettings>
    suspend fun updateSettings(settings: TelemetrySettings): Result<Unit>
    suspend fun setUserConsent(granted: Boolean): Result<Unit>
    suspend fun isConsentRequired(): Boolean
    
    // Recolección de eventos
    suspend fun recordEvent(event: TelemetryEvent): Result<Unit>
    suspend fun recordPerformanceMetric(metric: PerformanceMetrics): Result<Unit>
    suspend fun recordFeatureUsage(usage: FeatureUsage): Result<Unit>
    suspend fun recordStreamQuality(quality: StreamQualityMetrics): Result<Unit>
    
    // Gestión de datos
    suspend fun getPendingEvents(): List<TelemetryEvent>
    suspend fun createBatch(): TelemetryBatch?
    suspend fun markBatchAsSent(batchId: String): Result<Unit>
    suspend fun clearOldData(): Result<Unit>
    
    // Envío
    fun getUploadState(): Flow<TelemetryUploadState>
    suspend fun uploadBatch(batch: TelemetryBatch): Result<Unit>
    suspend fun scheduleUpload(): Result<Unit>
    
    // Privacidad
    suspend fun exportUserData(): Result<String>
    suspend fun deleteAllUserData(): Result<Unit>
    suspend fun getDataSummary(): Result<Map<String, Any>>
    
    // Sesión
    suspend fun startSession(): String
    suspend fun endSession(sessionId: String): Result<Unit>
    fun getCurrentSessionId(): String?
}