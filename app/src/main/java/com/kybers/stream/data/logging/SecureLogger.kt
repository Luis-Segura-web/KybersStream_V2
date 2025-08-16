package com.kybers.stream.data.logging

import android.util.Log
import com.kybers.stream.domain.model.TelemetryFilters
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Niveles de logging seguro
 */
enum class SecureLogLevel(val priority: Int) {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    CRITICAL(7)
}

/**
 * Configuración de logging
 */
data class LoggingConfig(
    val enableFileLogging: Boolean = false,
    val enableConsoleLogging: Boolean = true,
    val minLogLevel: SecureLogLevel = SecureLogLevel.INFO,
    val maxFileSizeMB: Int = 5,
    val maxFileCount: Int = 3,
    val enablePiiScrubbing: Boolean = true,
    val enableStackTraces: Boolean = true,
    val logRetentionDays: Int = 7
)

/**
 * Entrada de log sanitizada
 */
data class SecureLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: SecureLogLevel,
    val tag: String,
    val message: String,
    val sanitizedMessage: String,
    val threadName: String = Thread.currentThread().name,
    val className: String? = null,
    val methodName: String? = null,
    val lineNumber: Int? = null,
    val stackTrace: String? = null,
    val sessionId: String? = null
) {
    fun format(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val timestamp = dateFormat.format(Date(this.timestamp))
        val location = if (className != null && methodName != null && lineNumber != null) {
            " ($className.$methodName:$lineNumber)"
        } else ""
        
        return "$timestamp ${level.name.padEnd(7)} [$threadName] $tag$location: $sanitizedMessage"
    }
}

/**
 * Logger seguro que filtra automáticamente información sensible
 */
@Singleton
class SecureLogger @Inject constructor() {
    
    private val config = LoggingConfig()
    private val logQueue = ConcurrentLinkedQueue<SecureLogEntry>()
    private val logScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Patrones para detectar información sensible
    private val sensitivePatterns = listOf(
        // Credenciales y tokens
        Pattern.compile("(?i)(password|passwd|pwd|pass)\\s*[:=]\\s*\\S+"),
        Pattern.compile("(?i)(token|auth|bearer|api[_-]?key)\\s*[:=]\\s*\\S+"),
        Pattern.compile("(?i)(secret|credential|private[_-]?key)\\s*[:=]\\s*\\S+"),
        
        // URLs con credenciales
        Pattern.compile("(?i)://[^:/\\s]+:[^@/\\s]+@"),
        
        // Emails y números de teléfono
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"),
        Pattern.compile("\\b(?:\\+?1[-.]?)?(\\d{3})[-.]?(\\d{3})[-.]?(\\d{4})\\b"),
        
        // IPs y MACs
        Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"),
        Pattern.compile("\\b(?:[0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}\\b"),
        
        // IDs únicos y seriales
        Pattern.compile("\\b[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12}\\b"),
        Pattern.compile("\\b[A-Z0-9]{16,}\\b"),
        
        // Números de tarjeta de crédito
        Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b"),
        
        // Paths del sistema que pueden contener información sensible
        Pattern.compile("(?i)/data/data/[^/\\s]+/"),
        Pattern.compile("(?i)C:\\\\Users\\\\[^\\\\\\s]+"),
        
        // Claves de cifrado en formato base64
        Pattern.compile("\\b[A-Za-z0-9+/]{40,}={0,2}\\b")
    )
    
    // Patrones de reemplazo
    private val replacements = mapOf(
        "password" to "***PASSWORD***",
        "token" to "***TOKEN***",
        "secret" to "***SECRET***",
        "email" to "***EMAIL***",
        "phone" to "***PHONE***",
        "ip" to "***IP***",
        "mac" to "***MAC***",
        "uuid" to "***UUID***",
        "serial" to "***SERIAL***",
        "card" to "***CARD***",
        "path" to "***PATH***",
        "key" to "***KEY***"
    )
    
    init {
        // Iniciar worker para procesar logs
        startLogProcessor()
    }
    
    /**
     * Log verbose
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        log(SecureLogLevel.VERBOSE, tag, message, throwable)
    }
    
    /**
     * Log debug
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(SecureLogLevel.DEBUG, tag, message, throwable)
    }
    
    /**
     * Log info
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(SecureLogLevel.INFO, tag, message, throwable)
    }
    
    /**
     * Log warning
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(SecureLogLevel.WARN, tag, message, throwable)
    }
    
    /**
     * Log error
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(SecureLogLevel.ERROR, tag, message, throwable)
    }
    
    /**
     * Log crítico
     */
    fun c(tag: String, message: String, throwable: Throwable? = null) {
        log(SecureLogLevel.CRITICAL, tag, message, throwable)
    }
    
    /**
     * Log con información de contexto automática
     */
    fun logWithContext(level: SecureLogLevel, message: String, throwable: Throwable? = null) {
        val stackTrace = Thread.currentThread().stackTrace
        val caller = stackTrace.getOrNull(3) // Skip logWithContext, log, and current method
        
        val tag = caller?.className?.substringAfterLast('.') ?: "Unknown"
        val className = caller?.className
        val methodName = caller?.methodName
        val lineNumber = caller?.lineNumber
        
        log(level, tag, message, throwable, className, methodName, lineNumber)
    }
    
    /**
     * Método principal de logging
     */
    private fun log(
        level: SecureLogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        className: String? = null,
        methodName: String? = null,
        lineNumber: Int? = null,
        sessionId: String? = null
    ) {
        if (level.priority < config.minLogLevel.priority) {
            return
        }
        
        val sanitizedMessage = if (config.enablePiiScrubbing) {
            sanitizeMessage(message)
        } else {
            message
        }
        
        val stackTrace = if (config.enableStackTraces && throwable != null) {
            Log.getStackTraceString(throwable)
        } else null
        
        val logEntry = SecureLogEntry(
            level = level,
            tag = tag,
            message = message,
            sanitizedMessage = sanitizedMessage,
            className = className,
            methodName = methodName,
            lineNumber = lineNumber,
            stackTrace = stackTrace,
            sessionId = sessionId
        )
        
        // Agregar a cola para procesamiento asíncrono
        logQueue.offer(logEntry)
        
        // Log inmediato a consola si está habilitado
        if (config.enableConsoleLogging) {
            logToConsole(logEntry)
        }
    }
    
    /**
     * Sanitiza mensaje removiendo información sensible
     */
    private fun sanitizeMessage(message: String): String {
        var sanitized = message
        
        // Aplicar patrones de sanitización
        sensitivePatterns.forEach { pattern ->
            sanitized = pattern.matcher(sanitized).replaceAll { matchResult ->
                val match = matchResult.group()
                when {
                    match.contains("password", ignoreCase = true) -> replacements["password"]!!
                    match.contains("token", ignoreCase = true) -> replacements["token"]!!
                    match.contains("secret", ignoreCase = true) -> replacements["secret"]!!
                    match.contains("@") -> replacements["email"]!!
                    match.contains(":") && match.contains(".") -> replacements["ip"]!!
                    match.contains("-") && match.length > 10 -> replacements["uuid"]!!
                    else -> "***REDACTED***"
                }
            }
        }
        
        // Usar filtros de telemetría para anonimización adicional
        sanitized = TelemetryFilters.anonymizeUserData(sanitized)
        
        return sanitized
    }
    
    /**
     * Log a consola del sistema
     */
    private fun logToConsole(entry: SecureLogEntry) {
        val message = if (entry.stackTrace != null) {
            "${entry.sanitizedMessage}\n${entry.stackTrace}"
        } else {
            entry.sanitizedMessage
        }
        
        when (entry.level) {
            SecureLogLevel.VERBOSE -> Log.v(entry.tag, message)
            SecureLogLevel.DEBUG -> Log.d(entry.tag, message)
            SecureLogLevel.INFO -> Log.i(entry.tag, message)
            SecureLogLevel.WARN -> Log.w(entry.tag, message)
            SecureLogLevel.ERROR -> Log.e(entry.tag, message)
            SecureLogLevel.CRITICAL -> Log.e("CRITICAL_${entry.tag}", message)
        }
    }
    
    /**
     * Inicia el procesador de logs en segundo plano
     */
    private fun startLogProcessor() {
        logScope.launch {
            while (isActive) {
                try {
                    processLogQueue()
                    delay(1000) // Procesar cada segundo
                } catch (e: Exception) {
                    // Error en procesamiento, continuar sin interrumpir
                    Log.e("SecureLogger", "Error processing log queue", e)
                }
            }
        }
    }
    
    /**
     * Procesa la cola de logs
     */
    private suspend fun processLogQueue() {
        if (!config.enableFileLogging) return
        
        val logsToProcess = mutableListOf<SecureLogEntry>()
        
        // Extraer logs de la cola
        while (logQueue.isNotEmpty() && logsToProcess.size < 100) {
            logQueue.poll()?.let { logsToProcess.add(it) }
        }
        
        if (logsToProcess.isNotEmpty()) {
            writeLogsToFile(logsToProcess)
        }
    }
    
    /**
     * Escribe logs al archivo
     */
    private suspend fun writeLogsToFile(logs: List<SecureLogEntry>) {
        withContext(Dispatchers.IO) {
            try {
                val logFile = getLogFile()
                logFile.parentFile?.mkdirs()
                
                PrintWriter(FileWriter(logFile, true)).use { writer ->
                    logs.forEach { entry ->
                        writer.println(entry.format())
                        entry.stackTrace?.let { 
                            writer.println("Stack trace: $it")
                        }
                    }
                    writer.flush()
                }
                
                // Rotar archivos si es necesario
                rotateLogFiles()
                
            } catch (e: Exception) {
                Log.e("SecureLogger", "Error writing logs to file", e)
            }
        }
    }
    
    /**
     * Obtiene el archivo de log actual
     */
    private fun getLogFile(): File {
        val logDir = File("/data/data/com.kybers.stream/files/logs")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        return File(logDir, "app_$dateString.log")
    }
    
    /**
     * Rota archivos de log
     */
    private fun rotateLogFiles() {
        try {
            val logDir = File("/data/data/com.kybers.stream/files/logs")
            if (!logDir.exists()) return
            
            val logFiles = logDir.listFiles { file ->
                file.name.endsWith(".log")
            }?.sortedByDescending { it.lastModified() } ?: return
            
            // Eliminar archivos excedentes
            if (logFiles.size > config.maxFileCount) {
                logFiles.drop(config.maxFileCount).forEach { file ->
                    file.delete()
                }
            }
            
            // Eliminar archivos antiguos
            val cutoffTime = System.currentTimeMillis() - (config.logRetentionDays * 24 * 60 * 60 * 1000L)
            logFiles.filter { it.lastModified() < cutoffTime }.forEach { file ->
                file.delete()
            }
            
            // Verificar tamaño de archivos
            logFiles.forEach { file ->
                if (file.length() > config.maxFileSizeMB * 1024 * 1024) {
                    archiveLogFile(file)
                }
            }
            
        } catch (e: Exception) {
            Log.e("SecureLogger", "Error rotating log files", e)
        }
    }
    
    /**
     * Archiva un archivo de log grande
     */
    private fun archiveLogFile(file: File) {
        try {
            val timestamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
            val archivedFile = File(file.parent, "${file.nameWithoutExtension}_$timestamp.log")
            file.renameTo(archivedFile)
        } catch (e: Exception) {
            Log.e("SecureLogger", "Error archiving log file", e)
        }
    }
    
    /**
     * Genera hash de un mensaje para debugging sin exponer contenido
     */
    fun hashMessage(message: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(message.toByteArray())
            hash.fold("") { str, byte -> str + "%02x".format(byte) }.take(8)
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    /**
     * Limpia recursos al finalizar
     */
    fun cleanup() {
        logScope.cancel()
    }
}

/**
 * Extension functions para usar el logger fácilmente
 */
inline fun <reified T> T.logV(message: String, throwable: Throwable? = null) {
    SecureLogger().v(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logD(message: String, throwable: Throwable? = null) {
    SecureLogger().d(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logI(message: String, throwable: Throwable? = null) {
    SecureLogger().i(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logW(message: String, throwable: Throwable? = null) {
    SecureLogger().w(T::class.java.simpleName, message, throwable)
}

inline fun <reified T> T.logE(message: String, throwable: Throwable? = null) {
    SecureLogger().e(T::class.java.simpleName, message, throwable)
}