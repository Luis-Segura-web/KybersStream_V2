# Sistema de Caché con Expiración de 12 Horas - KybersStream V2

## Descripción
Este sistema implementa un mecanismo de caché inteligente que verifica la validez de los datos almacenados después del login y sincroniza automáticamente cuando es necesario.

## Características Principales

### ✅ Verificación Automática de Caché
- Al hacer login exitoso, se verifica si los datos en caché tienen menos de **12 horas**
- Si el caché es válido, se navega directamente a la pantalla principal
- Si el caché está expirado, se inicia la sincronización automática

### ✅ Ventana de Carga de Sincronización
- Muestra una ventana modal elegante durante la sincronización
- Barra de progreso con indicadores visuales
- Texto descriptivo de cada paso:
  - 📂 "Cargando categorías..."
  - 📺 "Cargando canales..."
  - 🎬 "Cargando películas..."
  - 📽️ "Cargando series..."
  - ✅ "Finalizando..."

### ✅ Manejo de Errores
- Botones de "Reintentar" y "Cancelar" en caso de error
- Mensajes de error descriptivos
- Posibilidad de cancelar la sincronización

### ✅ Datos Sincronizados
- **Canales de TV en vivo** con categorías
- **Películas VOD** con metadatos
- **Series** con información detallada
- **Categorías** organizadas por tipo

## Arquitectura Técnica

### Componentes Principales

1. **`LoginUserUseCase`**
   - Método `needsSync()` para verificar validez del caché
   - Integración con `DatabaseCacheManager`

2. **`SyncManager`**
   - Método `performInitialSyncWithCallback()` para progreso en tiempo real
   - Sincronización paralela de datos
   - Manejo de errores y reintentos

3. **`LoginViewModel`**
   - Estados de sincronización integrados
   - Manejo de callbacks de progreso
   - Control de la UI de carga

4. **`SyncDataDialog`**
   - Componente UI reutilizable
   - Animaciones y feedback visual
   - Botones de acción contextual

### Flujo de Trabajo

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│   Login     │───▶│  Verificar   │───▶│   Caché     │
│  Exitoso    │    │    Caché     │    │   Válido    │
└─────────────┘    └──────────────┘    └─────────────┘
                           │                    │
                           ▼                    ▼
                   ┌──────────────┐    ┌─────────────┐
                   │   Caché      │    │   Ir a      │
                   │  Expirado    │    │    Home     │
                   └──────────────┘    └─────────────┘
                           │
                           ▼
                   ┌──────────────┐
                   │   Mostrar    │
                   │  Ventana de  │
                   │    Carga     │
                   └──────────────┘
                           │
                           ▼
                   ┌──────────────┐
                   │ Sincronizar  │
                   │    Datos     │
                   └──────────────┘
```

## Configuración

### Tiempo de Expiración
```kotlin
// En DatabaseCacheManager.kt
private const val XTREAM_CACHE_HOURS = 12L
```

### Estados de Sincronización
```kotlin
enum class SyncStep(val stepNumber: Int) {
    STARTING(0),
    CATEGORIES(1), 
    CHANNELS(2),
    MOVIES(3),
    SERIES(4),
    FINISHING(5),
    COMPLETED(6),
    ERROR(-1)
}
```

## Uso

El sistema funciona automáticamente después del login. No requiere configuración adicional por parte del usuario.

### Para Desarrolladores

```kotlin
// Verificar si necesita sincronización
val needsSync = loginUserUseCase.needsSync(userProfile)

// Realizar sincronización con callback de progreso  
syncManager.performInitialSyncWithCallback { step, progress ->
    // Actualizar UI con el progreso
    updateSyncProgress(step, progress)
}
```

## Beneficios

1. **Mejor Experiencia de Usuario**: No hay retrasos innecesarios cuando los datos son recientes
2. **Eficiencia de Red**: Reduce el uso de datos móviles
3. **Rendimiento**: Carga más rápida cuando el caché es válido
4. **Feedback Visual**: El usuario siempre sabe qué está pasando
5. **Robustez**: Manejo de errores y opciones de recuperación

## Notas Técnicas

- El caché se almacena localmente usando Room Database
- Cada usuario tiene su propio hash único para el caché
- Los datos se limpian automáticamente cuando expiran
- El sistema funciona offline una vez que los datos están cacheados
