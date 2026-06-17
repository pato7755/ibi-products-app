package com.task.ibiproductsapp.presentation.login.addeditproduct

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.ibiproductsapp.common.NetworkResult
import com.task.ibiproductsapp.di.IoDispatcher
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.usecase.AddProductUseCase
import com.task.ibiproductsapp.domain.usecase.EditProductUseCase
import com.task.ibiproductsapp.domain.usecase.GetProductDetailUseCase
import com.task.ibiproductsapp.domain.usecase.ResetProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase,
    private val editProductUseCase: EditProductUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val resetProductUseCase: ResetProductUseCase,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val productId: Int? = savedStateHandle.get<Int>("productId")?.takeIf { it > 0 }

    private val _addEditState = MutableStateFlow(AddEditProductState(productId = productId))
    val addEditState = _addEditState.asStateFlow()

    init {
        if (productId != null) loadExistingProduct(productId)
    }

    private fun loadExistingProduct(id: Int) {
        _addEditState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { getProductDetailUseCase(id) }
            when (result) {
                is NetworkResult.Success -> {
                    val product = result.data
                    _addEditState.update {
                        it.copy(
                            isLoading = false,
                            title = product.title,
                            description = product.description,
                            price = product.price.toString(),
                            category = product.category,
                            brand = product.brand ?: "",
                            thumbnail = product.thumbnail,
                            images = product.images,
                            stock = product.stock.toString(),
                            isLocallyModified = product.isLocallyModified
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _addEditState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onTitleChanged(value: String) = _addEditState.update { it.copy(title = value, errorMessage = null) }
    fun onDescriptionChanged(value: String) = _addEditState.update { it.copy(description = value) }
    fun onPriceChanged(value: String) = _addEditState.update { it.copy(price = value, errorMessage = null) }
    fun onCategoryChanged(value: String) = _addEditState.update { it.copy(category = value) }
    fun onBrandChanged(value: String) = _addEditState.update { it.copy(brand = value) }
    fun onStockChanged(value: String) = _addEditState.update { it.copy(stock = value) }

    fun save() {
        val state = _addEditState.value
        val priceDouble = state.price.toDoubleOrNull()
        if (priceDouble == null || priceDouble <= 0) {
            _addEditState.update { it.copy(errorMessage = "Enter a valid price") }
            return
        }

        val product = Product(
            id = state.productId ?: generateLocalId(),
            title = state.title,
            description = state.description,
            price = priceDouble,
            category = state.category,
            brand = state.brand.ifBlank { null },
            stock = state.stock.toIntOrNull() ?: 0,
            discountPercentage = state.discountPercentage.toDoubleOrNull() ?: 0.0,
            rating = state.rating.toDoubleOrNull() ?: 0.0,
            thumbnail = state.thumbnail,
            images = state.images,
            tags = emptyList(),
            isLocallyModified = true
        )

        _addEditState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                if (state.productId != null) editProductUseCase(product)
                else addProductUseCase(product)
            }
            when (result) {
                is NetworkResult.Success -> _addEditState.update { it.copy(isLoading = false, isSaved = true) }
                is NetworkResult.Error -> _addEditState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun resetToApi() {
        val id = productId ?: return
        _addEditState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { resetProductUseCase(id) }
            when (result) {
                is NetworkResult.Success -> loadExistingProduct(id)
                is NetworkResult.Error -> _addEditState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    // Generate a negative local ID to avoid collisions with API IDs
    private fun generateLocalId(): Int = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
}