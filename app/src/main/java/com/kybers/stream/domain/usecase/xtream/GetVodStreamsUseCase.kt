package com.kybers.stream.domain.usecase.xtream

import com.kybers.stream.data.cache.DatabaseCacheManager
import com.kybers.stream.domain.model.Movie
import com.kybers.stream.domain.model.XtreamResult
import com.kybers.stream.domain.repository.UserRepository
import com.kybers.stream.domain.repository.XtreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetVodStreamsUseCase @Inject constructor(
    private val xtreamRepository: XtreamRepository,
    private val databaseCacheManager: DatabaseCacheManager,
    private val userRepository: UserRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<Result<List<Movie>>> = flow {
        try {
            val user = userRepository.getCurrentUser().first()
            if (user == null) {
                emit(Result.failure(Exception("Usuario no autenticado")))
                return@flow
            }
            
            val userHash = databaseCacheManager.generateUserHash(user.username, user.password, user.server)
            
            // Intentar obtener del cache primero
            val cachedMovies = databaseCacheManager.getCachedXtreamMovies(userHash)
            if (cachedMovies.isNotEmpty()) {
                emit(Result.success(cachedMovies))
                return@flow
            }
            
            // Si no hay cache, obtener del repositorio
            when (val result = xtreamRepository.getVodStreams(categoryId)) {
                is XtreamResult.Success -> {
                    // Cache the data
                    databaseCacheManager.cacheXtreamMovies(result.data, userHash)
                    emit(Result.success(result.data))
                }
                is XtreamResult.Error -> emit(Result.failure(Exception(result.message)))
                is XtreamResult.Loading -> {} // Loading state handled by UI
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}