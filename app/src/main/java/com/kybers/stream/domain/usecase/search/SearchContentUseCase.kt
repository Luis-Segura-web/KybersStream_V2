package com.kybers.stream.domain.usecase.search

import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class SearchResult {
    data class Channels(val channels: List<Channel>) : SearchResult()
    data class Movies(val movies: List<Movie>) : SearchResult()
    data class Series(val series: List<com.kybers.stream.domain.model.Series>) : SearchResult()
    data class Mixed(
        val channels: List<Channel> = emptyList(),
        val movies: List<Movie> = emptyList(),
        val series: List<com.kybers.stream.domain.model.Series> = emptyList()
    ) : SearchResult()
}

enum class SearchScope {
    ALL, LIVE_TV, MOVIES, SERIES
}

class SearchContentUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository
) {
    suspend operator fun invoke(
        query: String,
        scope: SearchScope = SearchScope.ALL,
        categoryId: String? = null
    ): Flow<Result<SearchResult>> = flow {
        if (query.isBlank()) {
            emit(Result.success(SearchResult.Mixed()))
            return@flow
        }
        
        try {
            when (scope) {
                SearchScope.LIVE_TV -> {
                    when (val result = xtreamRepository.getLiveStreams(categoryId)) {
                        is XtreamResult.Success -> {
                            val filtered = result.data.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            }
                            emit(Result.success(SearchResult.Channels(filtered)))
                        }
                        is XtreamResult.Error -> {
                            emit(Result.failure(Exception(result.message)))
                        }
                        is XtreamResult.Loading -> {
                            // Loading handled by UI
                        }
                    }
                }
                
                SearchScope.MOVIES -> {
                    when (val result = xtreamRepository.getVodStreams(categoryId)) {
                        is XtreamResult.Success -> {
                            val filtered = result.data.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            }
                            emit(Result.success(SearchResult.Movies(filtered)))
                        }
                        is XtreamResult.Error -> {
                            emit(Result.failure(Exception(result.message)))
                        }
                        is XtreamResult.Loading -> {
                            // Loading handled by UI
                        }
                    }
                }
                
                SearchScope.SERIES -> {
                    when (val result = xtreamRepository.getSeries(categoryId)) {
                        is XtreamResult.Success -> {
                            val filtered = result.data.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            }
                            emit(Result.success(SearchResult.Series(filtered)))
                        }
                        is XtreamResult.Error -> {
                            emit(Result.failure(Exception(result.message)))
                        }
                        is XtreamResult.Loading -> {
                            // Loading handled by UI
                        }
                    }
                }
                
                SearchScope.ALL -> {
                    // Búsqueda en todas las secciones
                    val channels = mutableListOf<Channel>()
                    val movies = mutableListOf<Movie>()
                    val series = mutableListOf<com.kybers.stream.domain.model.Series>()
                    
                    // Buscar en canales
                    when (val channelsResult = xtreamRepository.getLiveStreams()) {
                        is XtreamResult.Success -> {
                            channels.addAll(channelsResult.data.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            })
                        }
                        else -> {} // Ignorar errores en búsqueda global
                    }
                    
                    // Buscar en películas
                    when (val moviesResult = xtreamRepository.getVodStreams()) {
                        is XtreamResult.Success -> {
                            movies.addAll(moviesResult.data.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            })
                        }
                        else -> {} // Ignorar errores en búsqueda global
                    }
                    
                    // Buscar en series
                    when (val seriesResult = xtreamRepository.getSeries()) {
                        is XtreamResult.Success -> {
                            series.addAll(seriesResult.data.filter { 
                                it.name.contains(query, ignoreCase = true) 
                            })
                        }
                        else -> {} // Ignorar errores en búsqueda global
                    }
                    
                    emit(Result.success(SearchResult.Mixed(channels, movies, series)))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}