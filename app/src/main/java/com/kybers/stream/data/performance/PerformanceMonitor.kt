package com.kybers.stream.data.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Métricas de rendimiento
 */
data class PerformanceMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val memoryUsageMB: Long,
    val availableMemoryMB: Long,
    val cpuUsagePercent: Double,
    val frameDropCount: Int,
    val networkLatencyMs: Long,
    val diskUsageMB: Long,
    val threadCount: Int,
    val gcCount: Long,
    val batteryLevel: Int,
    val thermalState: String
)

/**
 * Estado de rendimiento
 */
sealed class PerformanceState {
    object Optimal : PerformanceState()
    object Good : PerformanceState()
    object Warning : PerformanceState()
    object Critical : PerformanceState()
}

/**
 * Monitor de rendimiento en tiempo real
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    private val _currentMetrics = MutableStateFlow(getCurrentMetrics())
    val currentMetrics: StateFlow<PerformanceMetrics> = _currentMetrics.asStateFlow()
    
    private val _performanceState = MutableStateFlow<PerformanceState>(PerformanceState.Optimal)
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    
    // Cache para optimizar cálculos
    private val metricsHistory = mutableListOf<PerformanceMetrics>()
    private var lastGcCount = 0L
    private var lastMemoryUsage = 0L
    private var frameDropCounter = 0
    
    /**
     * Inicia el monitoreo de rendimiento
     */
    fun startMonitoring(intervalMs: Long = 5000L) {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = scope.launch {
            while (isActive && isMonitoring) {
                try {
                    val metrics = getCurrentMetrics()
                    _currentMetrics.value = metrics
                    _performanceState.value = evaluatePerformanceState(metrics)
                    
                    updateMetricsHistory(metrics)
                    
                    delay(intervalMs)
                } catch (e: Exception) {
                    // Log error but continue monitoring
                    delay(intervalMs)
                }
            }
        }
    }
    
    /**
     * Detiene el monitoreo
     */
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    /**
     * Obtiene métricas actuales
     */
    private fun getCurrentMetrics(): PerformanceMetrics {
        val runtime = Runtime.getRuntime()
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        return PerformanceMetrics(
            memoryUsageMB = getMemoryUsageMB(),
            availableMemoryMB = memInfo.availMem / 1024 / 1024,
            cpuUsagePercent = getCpuUsage(),
            frameDropCount = frameDropCounter,
            networkLatencyMs = 0L, // Se actualizaría desde NetworkConnectivityManager
            diskUsageMB = getDiskUsageMB(),
            threadCount = Thread.activeCount(),
            gcCount = getGcCount(),
            batteryLevel = getBatteryLevel(),
            thermalState = getThermalState()
        )
    }
    
    /**
     * Calcula uso de memoria
     */
    private fun getMemoryUsageMB(): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / 1024 / 1024
    }
    
    /**
     * Estima uso de CPU
     */
    private fun getCpuUsage(): Double {
        return try {
            val startTime = System.nanoTime()
            val startCpuTime = Debug.threadCpuTimeNanos()
            
            // Pequeño trabajo para medir
            Thread.sleep(100)
            
            val endTime = System.nanoTime()
            val endCpuTime = Debug.threadCpuTimeNanos()
            
            val cpuTime = endCpuTime - startCpuTime
            val wallTime = endTime - startTime
            
            (cpuTime.toDouble() / wallTime.toDouble()) * 100.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    /**
     * Obtiene uso de disco
     */
    private fun getDiskUsageMB(): Long {
        return try {
            val cacheDir = context.cacheDir
            val usedSpace = getDirSize(cacheDir)
            usedSpace / 1024 / 1024
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Calcula tamaño de directorio
     */
    private fun getDirSize(dir: java.io.File): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles()
            files?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }
    
    /**
     * Obtiene contador de GC
     */
    private fun getGcCount(): Long {
        return try {
            // Using alternative method since getGlobalGcInvocationCount is deprecated
            val memInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memInfo)
            // Approximate GC count based on memory pressure changes
            val currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val memoryDiff = kotlin.math.abs(currentMemory - lastMemoryUsage)
            lastMemoryUsage = currentMemory
            
            // Simple heuristic: if memory changed significantly, likely a GC occurred
            if (memoryDiff > 1024 * 1024) { // 1MB threshold
                lastGcCount++
            }
            lastGcCount
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Obtiene nivel de batería
     */
    private fun getBatteryLevel(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1
        }
    }
    
    /**
     * Obtiene estado térmico
     */
    private fun getThermalState(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                when (powerManager.currentThermalStatus) {
                    android.os.PowerManager.THERMAL_STATUS_NONE -> "Normal"
                    android.os.PowerManager.THERMAL_STATUS_LIGHT -> "Ligero"
                    android.os.PowerManager.THERMAL_STATUS_MODERATE -> "Moderado"
                    android.os.PowerManager.THERMAL_STATUS_SEVERE -> "Severo"
                    android.os.PowerManager.THERMAL_STATUS_CRITICAL -> "Crítico"
                    android.os.PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergencia"
                    android.os.PowerManager.THERMAL_STATUS_SHUTDOWN -> "Apagado"
                    else -> "Desconocido"
                }
            } else {
                "No disponible"
            }
        } catch (e: Exception) {
            "Error"
        }
    }
    
    /**
     * Evalúa el estado de rendimiento
     */
    private fun evaluatePerformanceState(metrics: PerformanceMetrics): PerformanceState {
        var score = 100
        
        // Evaluar memoria
        val memoryUsagePercent = (metrics.memoryUsageMB.toDouble() / 
                                 (metrics.memoryUsageMB + metrics.availableMemoryMB)) * 100
        when {
            memoryUsagePercent > 90 -> score -= 30
            memoryUsagePercent > 80 -> score -= 20
            memoryUsagePercent > 70 -> score -= 10
        }
        
        // Evaluar CPU
        when {
            metrics.cpuUsagePercent > 80 -> score -= 25
            metrics.cpuUsagePercent > 60 -> score -= 15
            metrics.cpuUsagePercent > 40 -> score -= 5
        }
        
        // Evaluar frame drops
        when {
            metrics.frameDropCount > 10 -> score -= 20
            metrics.frameDropCount > 5 -> score -= 10
            metrics.frameDropCount > 2 -> score -= 5
        }
        
        // Evaluar GC frecuente
        if (metrics.gcCount > 3) score -= 15
        
        // Evaluar batería
        when {
            metrics.batteryLevel in 1..15 -> score -= 10
            metrics.batteryLevel in 16..30 -> score -= 5
        }
        
        // Evaluar estado térmico
        when (metrics.thermalState) {
            "Severo", "Crítico", "Emergencia" -> score -= 30
            "Moderado" -> score -= 15
            "Ligero" -> score -= 5
        }
        
        return when {
            score >= 85 -> PerformanceState.Optimal
            score >= 70 -> PerformanceState.Good
            score >= 50 -> PerformanceState.Warning
            else -> PerformanceState.Critical
        }
    }
    
    /**
     * Actualiza historial de métricas
     */
    private fun updateMetricsHistory(metrics: PerformanceMetrics) {
        metricsHistory.add(metrics)
        
        // Mantener solo las últimas 50 mediciones
        if (metricsHistory.size > 50) {
            metricsHistory.removeAt(0)
        }
    }
    
    /**
     * Obtiene promedio de métricas
     */
    fun getAverageMetrics(): PerformanceMetrics? {
        if (metricsHistory.isEmpty()) return null
        
        val count = metricsHistory.size.toLong()
        return PerformanceMetrics(
            memoryUsageMB = metricsHistory.sumOf { it.memoryUsageMB } / count,
            availableMemoryMB = metricsHistory.sumOf { it.availableMemoryMB } / count,
            cpuUsagePercent = metricsHistory.sumOf { it.cpuUsagePercent } / count,
            frameDropCount = (metricsHistory.sumOf { it.frameDropCount } / count).toInt(),
            networkLatencyMs = metricsHistory.sumOf { it.networkLatencyMs } / count,
            diskUsageMB = metricsHistory.sumOf { it.diskUsageMB } / count,
            threadCount = (metricsHistory.sumOf { it.threadCount } / count).toInt(),
            gcCount = metricsHistory.sumOf { it.gcCount } / count,
            batteryLevel = (metricsHistory.sumOf { it.batteryLevel } / count).toInt(),
            thermalState = metricsHistory.lastOrNull()?.thermalState ?: "Desconocido"
        )
    }
    
    /**
     * Fuerza garbage collection cuando es necesario
     */
    fun forceGarbageCollection() {
        if (_performanceState.value == PerformanceState.Critical) {
            System.gc()
        }
    }
    
    /**
     * Registra frame drop
     */
    fun recordFrameDrop() {
        frameDropCounter++
        
        // Resetear contador cada 10 segundos
        scope.launch {
            delay(10000)
            frameDropCounter = maxOf(0, frameDropCounter - 1)
        }
    }
    
    /**
     * Obtiene recomendaciones de optimización
     */
    fun getOptimizationRecommendations(): List<String> {
        val current = _currentMetrics.value
        val recommendations = mutableListOf<String>()
        
        val memoryUsagePercent = (current.memoryUsageMB.toDouble() / 
                                 (current.memoryUsageMB + current.availableMemoryMB)) * 100
        
        if (memoryUsagePercent > 80) {
            recommendations.add("Liberar caché de imágenes")
            recommendations.add("Cerrar streams inactivos")
        }
        
        if (current.cpuUsagePercent > 60) {
            recommendations.add("Reducir calidad de video")
            recommendations.add("Pausar tareas en segundo plano")
        }
        
        if (current.frameDropCount > 5) {
            recommendations.add("Habilitar aceleración por hardware")
            recommendations.add("Reducir resolución de pantalla")
        }
        
        if (current.gcCount > 2) {
            recommendations.add("Optimizar uso de memoria")
            recommendations.add("Reducir objetos temporales")
        }
        
        if (current.batteryLevel < 20) {
            recommendations.add("Activar modo de ahorro de energía")
            recommendations.add("Reducir brillo de pantalla")
        }
        
        if (current.thermalState in listOf("Moderado", "Severo", "Crítico")) {
            recommendations.add("Pausar actividad intensiva")
            recommendations.add("Reducir frecuencia de actualización")
        }
        
        return recommendations
    }
    
    /**
     * Limpia recursos
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
        metricsHistory.clear()
    }
}

/**
 * Manager de memoria con weak references
 */
@Singleton
class MemoryManager @Inject constructor() {
    
    private val imageCache = ConcurrentHashMap<String, WeakReference<Any>>()
    private val objectPool = ConcurrentHashMap<String, MutableList<Any>>()
    
    /**
     * Limpia caché de imágenes
     */
    fun clearImageCache() {
        imageCache.clear()
        System.gc()
    }
    
    /**
     * Limpia object pool
     */
    fun clearObjectPool() {
        objectPool.clear()
    }
    
    /**
     * Obtiene objeto del pool o crea uno nuevo
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getFromPool(key: String, factory: () -> T): T {
        val pool = objectPool.getOrPut(key) { mutableListOf() }
        
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1) as T
        } else {
            factory()
        }
    }
    
    /**
     * Devuelve objeto al pool
     */
    fun returnToPool(key: String, obj: Any) {
        val pool = objectPool.getOrPut(key) { mutableListOf() }
        if (pool.size < 10) { // Limitar tamaño del pool
            pool.add(obj)
        }
    }
    
    /**
     * Optimiza memoria cuando es crítica
     */
    fun optimizeMemory() {
        clearImageCache()
        
        // Limpiar pools grandes
        objectPool.values.forEach { pool ->
            if (pool.size > 5) {
                pool.subList(5, pool.size).clear()
            }
        }
        
        System.gc()
    }
}