# Integración TMDB en KybersStream

Esta documentación explica cómo usar la nueva funcionalidad de integración con TMDB (The Movie Database) para enriquecer la información de películas y series.

## 🎯 Características Principales

### ✅ Datos Enriquecidos
- **Información detallada**: Título original, sinopsis, géneros, productoras
- **Imágenes de alta calidad**: Pósters y fondos desde TMDB
- **Ratings y popularidad**: Puntuaciones de TMDB y conteo de votos
- **Metadatos adicionales**: Duración, fecha de estreno, idioma original
- **Cast y crew**: Información de actores y directores

### ✅ Funcionalidad Implementada
- Búsqueda mejorada en títulos originales y sinopsis
- Filtrado avanzado por géneros de TMDB
- Ordenamiento por popularidad y rating de TMDB
- Toggle para activar/desactivar datos TMDB
- Fallback a datos de Xtream cuando TMDB no está disponible

## 🏗️ Arquitectura

### Capas Implementadas

1. **API Layer** (`TMDBApi.kt`)
   - Interfaz Retrofit para comunicación con TMDB
   - Endpoints para películas y series
   - Manejo de imágenes y URLs

2. **Repository Layer** (`TMDBRepository.kt`, `TMDBRepositoryImpl.kt`)
   - Lógica de negocio para enriquecer contenido
   - Combinación de datos Xtream + TMDB
   - Manejo de errores y fallbacks

3. **Use Cases** (`TMDBUseCases.kt`)
   - `EnrichMovieUseCase`: Enriquecer película individual
   - `EnrichSeriesUseCase`: Enriquecer serie individual
   - `EnrichMoviesListUseCase`: Enriquecer lista de películas
   - `EnrichSeriesListUseCase`: Enriquecer lista de series

4. **ViewModels** (Actualizados)
   - `MoviesViewModel`: Soporte para películas enriquecidas
   - `SeriesViewModel`: Soporte para series enriquecidas
   - Estados de carga separados para TMDB

5. **UI Components** (`TMDBEnhancedCard.kt`)
   - `TMDBEnhancedMovieCard`: Tarjeta mejorada para películas
   - `TMDBEnhancedSeriesCard`: Tarjeta mejorada para series
   - `TMDBDataToggle`: Switch para activar/desactivar TMDB

## 🔧 Configuración

### Credenciales TMDB
Las credenciales están configuradas en `TMDBRepositoryImpl.kt`:

```kotlin
companion object {
    private const val TMDB_API_KEY = "0a82c6ff2b4b130f83facf56ae9a89b1"
    private const val LANGUAGE = "es-ES"
}
```

### Inyección de Dependencias
Los módulos Hilt están configurados en:
- `NetworkModule.kt`: APIs de Retrofit
- `RepositoryModule.kt`: Implementaciones de repositorios
- `UseCaseModule.kt`: Casos de uso

## 💻 Uso en ViewModels

### MoviesViewModel Actualizado

```kotlin
// El ViewModel ahora incluye estados para datos enriquecidos
data class MoviesUiState(
    val movies: List<Movie> = emptyList(),
    val enrichedMovies: List<EnrichedMovie> = emptyList(),
    val useTMDBData: Boolean = true,
    val isEnrichingWithTMDB: Boolean = false,
    // ... otros campos
)

// Métodos disponibles
fun toggleTMDBData() // Activar/desactivar TMDB
private fun enrichMoviesWithTMDB(movies: List<Movie>) // Enriquecer automáticamente
```

### SeriesViewModel Actualizado

```kotlin
// Similar al MoviesViewModel pero para series
data class SeriesUiState(
    val series: List<Series> = emptyList(),
    val enrichedSeries: List<EnrichedSeries> = emptyList(),
    val useTMDBData: Boolean = true,
    val isEnrichingWithTMDB: Boolean = false,
    // ... otros campos
)
```

## 🎨 Componentes UI

### TMDBEnhancedMovieCard

```kotlin
@Composable
fun TMDBEnhancedMovieCard(
    movie: EnrichedMovie,
    onMovieClick: (EnrichedMovie) -> Unit,
    onFavoriteClick: (String) -> Unit,
    isFavorite: Boolean = false,
    showTMDBData: Boolean = true,
    modifier: Modifier = Modifier
)
```

**Características:**
- Póster de alta calidad desde TMDB
- Rating con estrellas
- Géneros principales
- Año y duración
- Botón de favoritos
- Icono de reproducción

### TMDBEnhancedSeriesCard

```kotlin
@Composable
fun TMDBEnhancedSeriesCard(
    series: EnrichedSeries,
    onSeriesClick: (EnrichedSeries) -> Unit,
    onFavoriteClick: (String) -> Unit,
    isFavorite: Boolean = false,
    showTMDBData: Boolean = true,
    modifier: Modifier = Modifier
)
```

**Características similares a películas, más:**
- Número de temporadas y episodios
- Fechas de emisión
- Estado de producción

### TMDBDataToggle

```kotlin
@Composable
fun TMDBDataToggle(
    useTMDBData: Boolean,
    onToggle: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
)
```

Switch para activar/desactivar la funcionalidad TMDB con indicador de carga.

## 📱 Ejemplo de Implementación

### En una Pantalla de Películas

```kotlin
@Composable
fun MoviesScreen(viewModel: MoviesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    TMDBEnhancedMoviesGrid(
        movies = if (uiState.useTMDBData) {
            uiState.filteredEnrichedMovies
        } else {
            uiState.filteredMovies.map { it.toEnriched() }
        },
        useTMDBData = uiState.useTMDBData,
        isLoadingTMDBData = uiState.isEnrichingWithTMDB,
        onMovieClick = { movie -> 
            // Navegar a detalle
        },
        onFavoriteClick = { movieId -> 
            viewModel.toggleFavorite(movieId)
        },
        onToggleTMDBData = {
            viewModel.toggleTMDBData()
        },
        getFavoriteStatus = { movieId ->
            // Verificar si está en favoritos
            false
        }
    )
}
```

## 🔍 Búsqueda Mejorada

Con TMDB activado, la búsqueda incluye:
- Título original de TMDB
- Sinopsis/overview
- Nombres alternativos
- Géneros de TMDB

```kotlin
// En el ViewModel, la búsqueda automáticamente incluye datos TMDB
private fun applyFiltersToEnriched(enrichedMovies: List<EnrichedMovie>, ...): List<EnrichedMovie> {
    // Busca en título original, sinopsis, etc.
    filtered.filter { enrichedMovie ->
        enrichedMovie.name.contains(searchQuery, ignoreCase = true) ||
        enrichedMovie.tmdbData?.title?.contains(searchQuery, ignoreCase = true) == true ||
        enrichedMovie.tmdbData?.overview?.contains(searchQuery, ignoreCase = true) == true
    }
}
```

## 📊 Ordenamiento Avanzado

Nuevas opciones de ordenamiento disponibles:
- `popularity_desc`: Por popularidad de TMDB
- `rating_desc`: Por rating de TMDB (más preciso)
- `year_desc`: Por fecha de TMDB (más precisa)

## 🚀 Rendimiento

### Optimizaciones Implementadas
- **Carga asíncrona**: TMDB no bloquea la carga inicial
- **Carga en paralelo**: Múltiples películas/series se enriquecen simultáneamente
- **Manejo de errores**: Fallback a datos Xtream si TMDB falla
- **Cache de imágenes**: Coil maneja el cache automáticamente

### Estados de Carga
- `isLoading`: Carga inicial de Xtream
- `isEnrichingWithTMDB`: Carga de datos TMDB (no bloquea UI)

## 🔧 Personalización

### Idioma
Cambiar idioma en `TMDBRepositoryImpl.kt`:
```kotlin
private const val LANGUAGE = "es-ES" // Cambiar por "en-US", "fr-FR", etc.
```

### Tamaños de Imagen
Configurables en `TMDBApi.kt`:
```kotlin
const val POSTER_SIZE_W500 = "w500"        // Pósters
const val BACKDROP_SIZE_W1280 = "w1280"    // Fondos
const val PROFILE_SIZE_W185 = "w185"       // Perfiles
```

## 🐛 Manejo de Errores

La implementación incluye manejo robusto de errores:
- Timeout de red
- Respuestas vacías de TMDB
- IDs de TMDB inválidos
- Fallback automático a datos Xtream

## 📈 Próximas Mejoras

### Funcionalidades Planificadas
- [ ] Cache local de datos TMDB
- [ ] Sincronización periódica
- [ ] Detección automática de idioma
- [ ] Más tipos de contenido (documentales, etc.)
- [ ] Integración con trailers de YouTube

### Performance
- [ ] Paginación de requests TMDB
- [ ] Cache inteligente con expiración
- [ ] Precarga de datos populares

## 🧪 Testing

Para probar la funcionalidad:
1. Activar el toggle TMDB en la UI
2. Verificar que se muestran pósters de mejor calidad
3. Comprobar información adicional (géneros, ratings)
4. Probar búsqueda con títulos originales
5. Verificar fallback cuando TMDB está desactivado

## 📞 Soporte

Para problemas o mejoras:
- Revisar logs en caso de errores de red
- Verificar que las credenciales TMDB sean válidas
- Comprobar conectividad a internet
- Validar que los `tmdb_id` estén presentes en los datos Xtream