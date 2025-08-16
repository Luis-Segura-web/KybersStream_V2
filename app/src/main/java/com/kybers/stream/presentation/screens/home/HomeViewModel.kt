package com.kybers.stream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kybers.stream.domain.model.*
import com.kybers.stream.domain.usecase.favorites.GetFavoritesUseCase
import com.kybers.stream.domain.usecase.playback.GetContinueWatchingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val favorites: List<FavoriteItem> = emptyList(),
    val continueWatching: List<PlaybackProgress> = emptyList(),
    val recentContent: List<Any> = emptyList(), // Puede ser Channel, Movie, Series
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Combinar favoritos y continuar viendo
                combine(
                    getFavoritesUseCase(),
                    getContinueWatchingUseCase(limit = 10)
                ) { favorites, continueWatching ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            favorites = favorites.take(10), // Limitar a 10 favoritos más recientes
                            continueWatching = continueWatching,
                            error = null
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }
}