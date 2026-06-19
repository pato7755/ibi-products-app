package com.task.ibiproductsapp.presentation.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.di.IoDispatcher
import com.task.ibiproductsapp.domain.usecase.AddFavoriteUseCase
import com.task.ibiproductsapp.domain.usecase.GetFavoritesUseCase
import com.task.ibiproductsapp.domain.usecase.GetProductDetailUseCase
import com.task.ibiproductsapp.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val productId: Int = savedStateHandle.get<Int>("productId") ?: 0

    private val _detailState = MutableStateFlow(ProductDetailState())
    val detailState = _detailState.asStateFlow()

    init {
        loadProductDetails()
        viewModelScope.launch {
            getFavoritesUseCase().collect { favorites ->
                _detailState.update {
                    it.copy(isFavorite = favorites.any { fav -> fav.id == productId })
                }
            }
        }
    }

    fun loadProductDetails() {
        _detailState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                getProductDetailUseCase(productId)
            }
            when (result) {
                is NetworkResult.Success -> {
                    _detailState.update { it.copy(isLoading = false, product = result.data) }
                }
                is NetworkResult.Error -> {
                    _detailState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun toggleFavorite() {
        val product = _detailState.value.product ?: return
        val isFavorite = _detailState.value.isFavorite
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                if (isFavorite) removeFavoriteUseCase(product.id)
                else addFavoriteUseCase(product.id)
            }
            if (result is NetworkResult.Error) {
                _detailState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun retry() = loadProductDetails()
}