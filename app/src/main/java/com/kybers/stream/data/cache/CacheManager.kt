package com.kybers.stream.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entrada de caché con TTL
 */
data class CacheEntry<T>(
    val data: T,
    val timestamp: Long,
    val ttlMs: Long
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() - timestamp > ttlMs
    
    val remainingTtlMs: Long
        get() = maxOf(0, ttlMs - (System.currentTimeMillis() - timestamp))
}

/**
 * Estrategias de expiración de caché
 */
enum class CacheEvictionStrategy {
    LRU,        // Menos usado recientemente
    TTL_ONLY,   // Solo por tiempo
    SIZE_LIMIT  // Límite de tamaño
}

/**
 * Configuración de caché
 */
data class CacheConfig(
    val defaultTtlMs: Long = 5 * 60 * 1000L, // 5 minutos
    val maxSize: Int = 1000,
    val evictionStrategy: CacheEvictionStrategy = CacheEvictionStrategy.LRU,
    val cleanupIntervalMs: Long = 60 * 1000L // 1 minuto
)

/**
 * Manager de caché con TTL y múltiples estrategias
 */
@Singleton
class CacheManager @Inject constructor() {
    
    private val caches = ConcurrentHashMap<String, Cache<*, *>>()
    private val mutex = Mutex()
    
    /**
     * Obtiene o crea una instancia de caché
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <K, V> getCache(
        name: String,
        config: CacheConfig = CacheConfig()
    ): Cache<K, V> = mutex.withLock {
        caches.getOrPut(name) {
            Cache<K, V>(name, config)
        } as Cache<K, V>
    }
    
    /**
     * Limpia todas las cachés
     */
    suspend fun clearAll() = mutex.withLock {
        caches.values.forEach { it.clear() }
    }
    
    /**
     * Obtiene estadísticas de caché
     */
    suspend fun getStats(): Map<String, CacheStats> = mutex.withLock {
        caches.mapValues { (_, cache) -> cache.getStats() }
    }
    
    /**
     * Implementación de caché individual
     */
    class Cache<K, V>(
        private val name: String,
        private val config: CacheConfig
    ) {
        private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
        private val accessOrder = ConcurrentHashMap<K, Long>()
        private val mutex = Mutex()
        
        private var hits = 0L
        private var misses = 0L
        private var evictions = 0L
        
        /**
         * Obtiene un valor del caché
         */
        suspend fun get(key: K): V? = mutex.withLock {
            val entry = storage[key]
            
            if (entry == null) {
                misses++
                return null
            }
            
            if (entry.isExpired) {
                storage.remove(key)
                accessOrder.remove(key)
                misses++
                return null
            }
            
            // Actualizar orden de acceso para LRU
            accessOrder[key] = System.currentTimeMillis()
            hits++
            entry.data
        }
        
        /**
         * Almacena un valor en el caché
         */
        suspend fun put(key: K, value: V, ttlMs: Long = config.defaultTtlMs) = mutex.withLock {
            val entry = CacheEntry(value, System.currentTimeMillis(), ttlMs)
            storage[key] = entry
            accessOrder[key] = System.currentTimeMillis()
            
            // Aplicar estrategia de evicción si es necesario
            if (storage.size > config.maxSize) {
                evictEntries()
            }
        }
        
        /**
         * Elimina una entrada específica
         */
        suspend fun remove(key: K): V? = mutex.withLock {
            accessOrder.remove(key)
            storage.remove(key)?.data
        }
        
        /**
         * Verifica si existe una clave válida
         */
        suspend fun containsKey(key: K): Boolean = mutex.withLock {
            val entry = storage[key]
            entry != null && !entry.isExpired
        }
        
        /**
         * Limpia todas las entradas
         */
        suspend fun clear() = mutex.withLock {
            storage.clear()
            accessOrder.clear()
        }
        
        /**
         * Limpia entradas expiradas
         */
        suspend fun cleanupExpired() = mutex.withLock {
            val expiredKeys = storage.entries
                .filter { it.value.isExpired }
                .map { it.key }
            
            expiredKeys.forEach { key ->
                storage.remove(key)
                accessOrder.remove(key)
            }
            
            evictions += expiredKeys.size
        }
        
        /**
         * Obtiene o calcula un valor
         */
        suspend fun getOrPut(
            key: K, 
            ttlMs: Long = config.defaultTtlMs,
            producer: suspend () -> V
        ): V {
            get(key)?.let { return it }
            
            val value = producer()
            put(key, value, ttlMs)
            return value
        }
        
        /**
         * Obtiene estadísticas
         */
        fun getStats(): CacheStats {
            val totalRequests = hits + misses
            val hitRate = if (totalRequests > 0) hits.toDouble() / totalRequests else 0.0
            
            return CacheStats(
                name = name,
                size = storage.size,
                maxSize = config.maxSize,
                hits = hits,
                misses = misses,
                evictions = evictions,
                hitRate = hitRate
            )
        }
        
        private fun evictEntries() {
            when (config.evictionStrategy) {
                CacheEvictionStrategy.LRU -> evictLRU()
                CacheEvictionStrategy.TTL_ONLY -> evictExpired()
                CacheEvictionStrategy.SIZE_LIMIT -> evictOldest()
            }
        }
        
        private fun evictLRU() {
            val entriesToRemove = storage.size - (config.maxSize * 0.8).toInt()
            if (entriesToRemove <= 0) return
            
            val lruKeys = accessOrder.entries
                .sortedBy { it.value }
                .take(entriesToRemove)
                .map { it.key }
            
            lruKeys.forEach { key ->
                storage.remove(key)
                accessOrder.remove(key)
                evictions++
            }
        }
        
        private fun evictExpired() {
            val expiredKeys = storage.entries
                .filter { it.value.isExpired }
                .map { it.key }
            
            expiredKeys.forEach { key ->
                storage.remove(key)
                accessOrder.remove(key)
                evictions++
            }
        }
        
        private fun evictOldest() {
            val entriesToRemove = storage.size - (config.maxSize * 0.8).toInt()
            if (entriesToRemove <= 0) return
            
            val oldestKeys = storage.entries
                .sortedBy { it.value.timestamp }
                .take(entriesToRemove)
                .map { it.key }
            
            oldestKeys.forEach { key ->
                storage.remove(key)
                accessOrder.remove(key)
                evictions++
            }
        }
    }
}

/**
 * Estadísticas de rendimiento de caché
 */
data class CacheStats(
    val name: String,
    val size: Int,
    val maxSize: Int,
    val hits: Long,
    val misses: Long,
    val evictions: Long,
    val hitRate: Double
) {
    val description: String
        get() = buildString {
            appendLine("Caché '$name':")
            appendLine("  Tamaño: $size/$maxSize")
            appendLine("  Hits: $hits")
            appendLine("  Misses: $misses")
            appendLine("  Tasa de aciertos: ${(hitRate * 100).toInt()}%")
            appendLine("  Evictions: $evictions")
        }
}

/**
 * Configuraciones predefinidas de caché
 */
object CacheConfigs {
    
    val METADATA = CacheConfig(
        defaultTtlMs = 10 * 60 * 1000L, // 10 minutos
        maxSize = 500,
        evictionStrategy = CacheEvictionStrategy.LRU
    )
    
    val CATEGORIES = CacheConfig(
        defaultTtlMs = 30 * 60 * 1000L, // 30 minutos
        maxSize = 100,
        evictionStrategy = CacheEvictionStrategy.TTL_ONLY
    )
    
    val EPG = CacheConfig(
        defaultTtlMs = 15 * 60 * 1000L, // 15 minutos
        maxSize = 1000,
        evictionStrategy = CacheEvictionStrategy.LRU
    )
    
    val THUMBNAILS = CacheConfig(
        defaultTtlMs = 60 * 60 * 1000L, // 1 hora
        maxSize = 200,
        evictionStrategy = CacheEvictionStrategy.SIZE_LIMIT
    )
    
    val SHORT_LIVED = CacheConfig(
        defaultTtlMs = 2 * 60 * 1000L, // 2 minutos
        maxSize = 100,
        evictionStrategy = CacheEvictionStrategy.TTL_ONLY
    )
}