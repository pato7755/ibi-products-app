package com.task.ibiproductsapp.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.ibiproductsapp.di.IoDispatcher
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

    // Holds the pending removal jobs so we can cancel them on undo
    private val removalJobs = mutableMapOf<Int, Job>()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase().collect { favorites ->
                _favoritesState.update { state ->
                    val currentIds = favorites.map { it.id }.toSet()
                    state.copy(
                        favorites = favorites,
                        isLoading = false,
                        // Drop any pending IDs that Room confirms are already gone
                        pendingRemovalIds = state.pendingRemovalIds.intersect(currentIds)
                    )
                }
            }
        }
    }

    fun removeFavorite(productId: Int) {
        removalJobs[productId]?.cancel()
        _favoritesState.update {
            it.copy(pendingRemovalIds = it.pendingRemovalIds + productId)
        }

        removalJobs[productId] = viewModelScope.launch {
            delay(4000)
            withContext(ioDispatcher) {
                removeFavoriteUseCase(productId)
            }
            removalJobs.remove(productId)
        }
    }

    fun undoRemove(productId: Int) {
        removalJobs[productId]?.cancel()
        removalJobs.remove(productId)
        _favoritesState.update {
            it.copy(pendingRemovalIds = it.pendingRemovalIds - productId)
        }
    }
}
