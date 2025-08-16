package com.kybers.stream.data.debug

import android.os.Build
import android.os.StrictMode
import com.kybers.stream.BuildConfig

/**
 * Helper para configurar StrictMode en builds de debug
 * Ayuda a detectar problemas de rendimiento y threading
 */
object StrictModeHelper {
    
    /**
     * Configura StrictMode para desarrollo
     */
    fun enableStrictMode() {
        if (!BuildConfig.DEBUG) {
            return // Solo en debug builds
        }
        
        setupThreadPolicy()
        setupVmPolicy()
    }
    
    /**
     * Configura políticas de thread
     */
    private fun setupThreadPolicy() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll() // Detectar todas las violaciones
            .penaltyLog() // Log de violaciones
            
        // Configuraciones específicas por versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            threadPolicy
                .detectResourceMismatches() // Detectar recursos mal usados
                .detectUnbufferedIo() // Detectar IO sin buffer
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            threadPolicy.detectUnbufferedIo()
        }
        
        // Solo dialog en desarrollo local, no en CI/CD
        if (isLocalDevelopment()) {
            threadPolicy.penaltyFlashScreen()
        }
        
        StrictMode.setThreadPolicy(threadPolicy.build())
    }
    
    /**
     * Configura políticas de VM
     */
    private fun setupVmPolicy() {
        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectAll() // Detectar todas las violaciones
            .penaltyLog() // Log de violaciones
            
        // Configuraciones específicas por versión
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            vmPolicy
                .detectCleartextNetwork() // Detectar tráfico HTTP no encriptado
                .detectFileUriExposure() // Detectar URIs de archivo expuestas
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vmPolicy
                .detectContentUriWithoutPermission() // Detectar URIs sin permisos
                .detectUntaggedSockets() // Detectar sockets sin etiqueta
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            vmPolicy
                .detectNonSdkApiUsage() // Detectar uso de APIs no públicas
                .detectImplicitDirectBoot() // Detectar problemas de arranque directo
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vmPolicy.detectCredentialProtectedWhileLocked()
        }
        
        // Solo dialog en desarrollo local
        if (isLocalDevelopment()) {
            vmPolicy.penaltyLog()
        }
        
        StrictMode.setVmPolicy(vmPolicy.build())
    }
    
    /**
     * Configura StrictMode específico para testing
     */
    fun enableStrictModeForTesting() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
            
        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
            
        StrictMode.setThreadPolicy(threadPolicy)
        StrictMode.setVmPolicy(vmPolicy)
    }
    
    /**
     * Configura StrictMode relajado para casos específicos
     */
    fun enableRelaxedStrictMode() {
        if (!BuildConfig.DEBUG) {
            return
        }
        
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectNetwork() // Solo detectar red en main thread
            .detectDiskReads() // Solo detectar lecturas de disco
            .penaltyLog()
            .build()
            
        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .penaltyLog()
            .build()
            
        StrictMode.setThreadPolicy(threadPolicy)
        StrictMode.setVmPolicy(vmPolicy)
    }
    
    /**
     * Deshabilita StrictMode temporalmente
     */
    fun disableStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }
    
    /**
     * Permite operación específica sin violación de StrictMode
     */
    inline fun <T> permitDiskReads(action: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskReads()
        return try {
            action()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
    
    /**
     * Permite escritura a disco sin violación
     */
    inline fun <T> permitDiskWrites(action: () -> T): T {
        val oldPolicy = StrictMode.allowThreadDiskWrites()
        return try {
            action()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
    
    /**
     * Permite ambas operaciones de disco
     */
    inline fun <T> permitDiskOperations(action: () -> T): T {
        return permitDiskReads {
            permitDiskWrites {
                action()
            }
        }
    }
    
    /**
     * Detecta si estamos en desarrollo local vs CI/CD
     */
    private fun isLocalDevelopment(): Boolean {
        // Métodos para detectar si estamos en desarrollo local
        return try {
            // Verificar variables de entorno comunes de CI/CD
            val ciEnvironments = listOf(
                "CI", "CONTINUOUS_INTEGRATION", "BUILD_NUMBER",
                "JENKINS_URL", "GITHUB_ACTIONS", "GITLAB_CI",
                "TRAVIS", "CIRCLECI", "BITBUCKET_BUILD_NUMBER"
            )
            
            ciEnvironments.none { 
                System.getenv(it) != null || System.getProperty(it) != null 
            }
        } catch (e: Exception) {
            true // Asumir desarrollo local si no podemos detectar
        }
    }
    
    /**
     * Configuración específica para diferentes tipos de build
     */
    fun configureForBuildType(buildType: String) {
        when (buildType.lowercase()) {
            "debug" -> enableStrictMode()
            "staging" -> enableRelaxedStrictMode()
            "release" -> disableStrictMode()
            else -> enableRelaxedStrictMode()
        }
    }
    
    /**
     * Reporta estadísticas de violaciones de StrictMode
     */
    fun getStrictModeViolationStats(): Map<String, Int> {
        // En una implementación real, podríamos trackear violaciones
        return mapOf(
            "threadViolations" to 0,
            "vmViolations" to 0,
            "networkOnMainThread" to 0,
            "diskOperationsOnMainThread" to 0
        )
    }
}

/**
 * Annotation para marcar métodos que intencionalmente violan StrictMode
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class StrictModeViolation(val reason: String)

/**
 * Extension para usar StrictMode de manera más fácil
 */
inline fun <T> strictModePermit(
    diskReads: Boolean = false,
    diskWrites: Boolean = false,
    action: () -> T
): T {
    return when {
        diskReads && diskWrites -> StrictModeHelper.permitDiskOperations(action)
        diskReads -> StrictModeHelper.permitDiskReads(action)
        diskWrites -> StrictModeHelper.permitDiskWrites(action)
        else -> action()
    }
}