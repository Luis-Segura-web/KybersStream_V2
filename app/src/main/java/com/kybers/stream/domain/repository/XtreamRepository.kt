package com.kybers.stream.domain.repository

import com.kybers.stream.domain.model.*
import kotlinx.coroutines.flow.Flow

interface XtreamRepository {
    // Categorías
    suspend fun getLiveCategories(): XtreamResult<List<Category>>
    suspend fun getVodCategories(): XtreamResult<List<Category>>
    suspend fun getSeriesCategories(): XtreamResult<List<Category>>
    
    // Contenido por categoría
    suspend fun getLiveStreams(categoryId: String? = null): XtreamResult<List<Channel>>
    suspend fun getVodStreams(categoryId: String? = null): XtreamResult<List<Movie>>
    suspend fun getSeries(categoryId: String? = null): XtreamResult<List<Series>>
    
    // Detalles específicos
    suspend fun getVodInfo(vodId: String): XtreamResult<MovieDetail>
    suspend fun getSeriesInfo(seriesId: String): XtreamResult<SeriesDetail>
    
    // EPG
    suspend fun getShortEpg(streamId: String, limit: Int = 10): XtreamResult<EpgResponse>
    suspend fun getXmlEpg(): XtreamResult<String>
}