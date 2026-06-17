package com.task.ibiproductsapp.domain.usecase

import androidx.paging.PagingData
import com.task.ibiproductsapp.domain.model.Product
import com.task.ibiproductsapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(
        searchQuery: String = "",
        category: String? = null,
        sortOption: String = "default"
    ): Flow<PagingData<Product>> =
        productRepository.getPagedProducts(searchQuery, category, sortOption)
}