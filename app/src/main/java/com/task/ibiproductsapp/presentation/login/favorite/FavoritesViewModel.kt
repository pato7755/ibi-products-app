package com.task.ibiproductsapp.presentation.login.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.ibiproductsapp.di.IoDispatcher
import com.task.ibiproductsapp.domain.usecase.AddFavoriteUseCase
import com.task.ibiproductsapp.domain.usecase.GetFavoritesUseCase
import com.task.ibiproductsapp.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _favoritesState = MutableStateFlow(FavoritesState())
    val favoritesState = _favoritesState.asStateFlow()

    // Holds the pending removal job so we can cancel it on undo
    private var removalJob: Job? = null

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            _favoritesState.update { it.copy(isLoading = true) }
            getFavoritesUseCase().collect { favorites ->
                _favoritesState.update { it.copy(favorites = favorites, isLoading = false) }
            }
        }
    }

    fun removeFavorite(productId: Int) {
        removalJob?.cancel()
        _favoritesState.update {
            it.copy(pendingRemovalIds = it.pendingRemovalIds + productId)
        }

        removalJob = viewModelScope.launch {
            delay(4000)
            withContext(ioDispatcher) {
                removeFavoriteUseCase(productId)
            }
            _favoritesState.update {
                it.copy(pendingRemovalIds = it.pendingRemovalIds - productId)
            }
        }
    }

    fun undoRemove() {
        removalJob?.cancel()
        removalJob = null
        _favoritesState.update { it.copy(pendingRemovalIds = emptySet()) }
    }
}
