package com.task.ibiproductsapp.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.task.ibiproductsapp.di.IoDispatcher
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.usecase.AddFavoriteUseCase
import com.task.ibiproductsapp.domain.usecase.DeleteProductUseCase
import com.task.ibiproductsapp.domain.usecase.GetCategoriesUseCase
import com.task.ibiproductsapp.domain.usecase.GetFavoritesUseCase
import com.task.ibiproductsapp.domain.usecase.GetProductsUseCase
import com.task.ibiproductsapp.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _productState = MutableStateFlow(ProductState())
    val productState = _productState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    val searchParams = MutableStateFlow(SearchParams())

    val products: Flow<PagingData<Product>> = searchParams
        .flatMapLatest { params ->
            getProductsUseCase(
                params.searchQuery,
                params.category,
                params.sortOption
            )
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    searchParams.update { it.copy(searchQuery = query) }
                }
        }
        observeCategories()
        observeFavoriteIds()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _productState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun observeFavoriteIds() {
        viewModelScope.launch {
            getFavoritesUseCase().collect { favorites ->
                _productState.update {
                    it.copy(favoriteIds = favorites.map { it -> it.id }.toSet())
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _productState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun onCategorySelected(category: String?) {
        _productState.update { it.copy(selectedCategory = category) }
        searchParams.update { it.copy(category = category) }
    }

    fun onSortOptionSelected(sort: String) {
        _productState.update { it.copy(sortOption = sort) }
        searchParams.update { it.copy(sortOption = sort) }
    }

    fun toggleFavorite(product: Product) {
        val isFavorite = _productState.value.favoriteIds.contains(product.id)
        viewModelScope.launch {
            withContext(ioDispatcher) {
                if (isFavorite) removeFavoriteUseCase(product.id)
                else addFavoriteUseCase(product.id)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            _productState.update { it.copy(recentlyDeletedProduct = product) }
            withContext(ioDispatcher) {
                deleteProductUseCase(product.id)
            }
        }
    }

    fun clearError() {
        _productState.update { it.copy(errorMessage = null) }
    }
}